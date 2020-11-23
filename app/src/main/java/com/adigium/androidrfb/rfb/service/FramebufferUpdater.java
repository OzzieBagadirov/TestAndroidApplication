package com.adigium.androidrfb.rfb.service;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.adigium.androidrfb.rfb.encoding.EncodingInterface;
import com.adigium.androidrfb.rfb.encoding.Encodings;
import com.adigium.androidrfb.rfb.encoding.HextileEncoder;
import com.adigium.androidrfb.rfb.encoding.RawEncoder;
import com.adigium.androidrfb.rfb.encoding.RichCursorEncoder;
import com.adigium.androidrfb.rfb.encoding.Tile;
import com.adigium.androidrfb.rfb.image.TrueColorImage;
import com.adigium.androidrfb.rfb.screen.ScreenCapture;


public class FramebufferUpdater implements Runnable {

	public static final long DELAY = 15;
	public static int imageReady = 0;

	private OutputStream out;

	private boolean running;

	private BlockingQueue<FramebufferUpdateRequest> updateRequests;

	private final CountDownLatch latch;

	private final ClientHandler clientHandler;

	private boolean loadingState;

	private boolean richCursorSent;

	private int[] clientEncodings;

	private int[] preferredEncodings;

	private EncodingInterface lastEncoder;

	private List<Tile> lastImage;

	private SetPixelFormat pixelFormat;

	public FramebufferUpdater(final ClientHandler clientHandler, final OutputStream out) {

		this.clientHandler = clientHandler;
		this.out = out;

		this.running = false;
		
		this.updateRequests = new LinkedBlockingQueue<>();
	
		this.loadingState = true;
		this.richCursorSent = true;

		this.lastEncoder = null;
				
		// Initially support only RAW encoding.
		this.clientEncodings = new int[] {Encodings.RAW};
		
		this.lastImage = null;
		
		// Default pixel format, 32-bit true image.
		this.pixelFormat = SetPixelFormat.default32bit();
		
		this.latch = new CountDownLatch(1);
	}

	public void setPixelFormat(final SetPixelFormat newPixelFormat) {
		this.pixelFormat = newPixelFormat;
		this.richCursorSent = false; // This will cause main loop to (re)send cursor information.
	}

	public void update(final FramebufferUpdateRequest request) throws InterruptedException {
		this.updateRequests.put(request);
	}

	public void setClientEncodings(final SetEncodings setEncodingsRequest) {
		this.clientEncodings = setEncodingsRequest.encodingType;		
	}

	public void setPreferredEncodings(final int[] preferredEncodings) {
		this.preferredEncodings = preferredEncodings;		
	}
	
	@Override
	public void run() {

		this.running = true;
		this.latch.countDown();
		
		try {
			
			while (this.running) {

				final FramebufferUpdateRequest updateRequest = this.updateRequests.poll(DELAY, TimeUnit.MILLISECONDS);

				// Here be careful to check updateRequest object against null value,
				//  since frame buffer updater thread is started in parallel with client handler thread.
				if (updateRequest == null) {
					continue;
				}
				
				if (!this.loadingState) {
					Log.d("FramebufferUpdater", "Loading state");
										
//					this.framebufferUpdateLoading();

					this.loadingState = false;
					
					// TimeUnit.SECONDS.sleep(4); // Give user few seconds to read welcome message.
				}
				else if (!this.richCursorSent && SelectEncoder.containsEncoding(Encodings.RICH_CURSOR, this.clientEncodings)) {
					Log.d("FramebufferUpdater", "Rich cursor sent");

					this.framebufferUpdateRichCursor();
					
					this.richCursorSent = true;
				}
				else {
					//
					// Now create new frame buffer update message, and write to socket.
					//
					try {
						boolean updated = this.framebufferUpdate(updateRequest);

						// It might happen that method returns false, if screen image did not change.
						if (!updated) {

							// Put back frame buffer update request in queue.
							// Take it again after DELAY time period.
							this.updateRequests.put(updateRequest);
						}
					} catch (Exception e) {
						Log.e("FramebufferUpdater", "Exception ", e);
					}
				}				
						
				TimeUnit.MILLISECONDS.sleep(DELAY);
			}
		}
		catch (final Exception exception) {
			
			// On any problem, just terminate this thread.
		}
		
		this.running = false;
		this.clientHandler.terminate();
	}


	private TrueColorImage getScreenImage() {
		return ScreenCapture.getScreenshot();
	}


	public static void imageReady() {
		imageReady += 1;
	}
	

	private List<Tile> getChangedTiles() {

		final TrueColorImage image = getScreenImage();

		if (image != null) {
			if (this.lastImage == null) {

				this.lastImage = Tile.build(image.raw, image.width, image.height);

				return this.lastImage;
			}

			final List<Tile> newImage = Tile.build(image.raw, image.width, image.height);

			if (newImage.size() != this.lastImage.size()) {

				this.lastImage = newImage;

				return this.lastImage;
			}

			final List<Tile> result = new ArrayList<>();

			for (int index = 0; index < newImage.size(); index++) {

				if (this.lastImage.get(index).equals(newImage.get(index)) == false) {

					// Found tile that's changed.
					result.add(newImage.get(index));
				}
			}

			// Update reference with current screen image.
			this.lastImage = newImage;

			return result;
		} else {
			return new ArrayList<Tile>();
		}
	}
	


	private EncodingInterface selectEncoder() {
		
		this.lastEncoder = SelectEncoder.selectEncoder(this.lastEncoder, this.clientEncodings, this.preferredEncodings);
		
		return this.lastEncoder;
	}	


	private boolean framebufferUpdate(final FramebufferUpdateRequest updateRequest) throws IOException {

		// Find suitable encoder for frame buffer update response.
		final EncodingInterface encoder = selectEncoder();
		
		// Indicator if VNC client needs full frame buffer data (screen image).
		final boolean fullUpdate = (updateRequest.incremental == 0);
		
		if (fullUpdate) {

			// This will enforce method getChangedTiles() to return list with complete screen image.
			this.lastImage = null;
		}
		
		//
		// Take current image of screen,
		// as list of tiles.
		// Update only tiles that are different comparing to last invocation.
		//


		final List<Tile> tiles = getChangedTiles();

		if (tiles.isEmpty()) {
			return false;
		}

		final DataOutputStream dataOut = new DataOutputStream(this.out);
		
		dataOut.write(0); // FrameBufferUpdate message type.
		dataOut.write(0); // Padding.

		short numberOfRectangles = (short) tiles.size();
		
		dataOut.writeShort(numberOfRectangles);

		Date startDateTiles = new Date();
		for (final Tile tile : tiles) {
//			Date startDate = new Date();
			dataOut.writeShort(tile.xPos);
			dataOut.writeShort(tile.yPos);
			dataOut.writeShort(tile.width);
			dataOut.writeShort(tile.height);
			dataOut.writeInt(encoder.getType());
			final byte[] encodedImage = encoder.encode(tile.raw(), tile.width, tile.height, this.pixelFormat);
			dataOut.write(encodedImage);
//			Date endDate = new Date();
//			Log.d("FRAMEBUFFER", "Encoding took " + (endDate.getTime() - startDate.getTime()) + " ms");

		}
		Date endDateTiles = new Date();
		Log.d("FRAMEBUFFER", "All " + tiles.size() + " tiles took " + (endDateTiles.getTime() - startDateTiles.getTime()) + " ms");
		dataOut.flush();
		
		return true;
	}


	private void framebufferUpdateLoading() throws IOException {
				
		final int width, height;

		width = ScreenCapture.screenWidth;
		height = ScreenCapture.screenHeight;
		
//		final TrueColorImage loadingImage = LoadingResource.get(width, height);
		
		final DataOutputStream dataOut = new DataOutputStream(this.out);
		
		dataOut.write(0); // FrameBufferUpdate message type.
		dataOut.write(0); // Padding.
		
		short numberOfRectangles = 1;
		
		dataOut.writeShort(numberOfRectangles);
			
		final short xPos = 0, yPos = 0;
		
		dataOut.writeShort(xPos);
		dataOut.writeShort(yPos);
		dataOut.writeShort(width);
		dataOut.writeShort(height);		
			
		this.lastEncoder = SelectEncoder.selectEncoder(this.lastEncoder, this.clientEncodings, this.preferredEncodings);
		dataOut.writeInt(this.lastEncoder.getType());
		
//		dataOut.write(this.lastEncoder.encode(loadingImage.raw, loadingImage.width, loadingImage.height, this.pixelFormat));
		
		dataOut.flush();
	}
	

	private void framebufferUpdateRichCursor() throws IOException {
		
		final RichCursorEncoder encoder = new RichCursorEncoder();
		
		final byte[] cursorData = encoder.encode(null, 0, 0, pixelFormat);
		
		if (cursorData == null) {
			
			return;
		}
		
		final DataOutputStream dataOut = new DataOutputStream(this.out);
		
		dataOut.write(0); // FrameBufferUpdate message type.
		dataOut.write(0); // Padding.
		
		short numberOfRectangles = 1;
		
		dataOut.writeShort(numberOfRectangles);
			
		short xPos = 0, yPos = 0
			, width = 18
			, height = 18; // Depends on 'cursor*.raw' captured data.
		
		dataOut.writeShort(xPos);
		dataOut.writeShort(yPos);
		dataOut.writeShort(width);
		dataOut.writeShort(height);
		dataOut.writeInt(Encodings.RICH_CURSOR);
		
		dataOut.write(cursorData);
		
		dataOut.flush();
	}
	

	public void start() throws InterruptedException {
		
		final Thread frameBufferUpdateThread = new Thread(this,
				String.format("%s-[%s]",
						FramebufferUpdater.class.getSimpleName(), this.clientHandler.toString()));
		frameBufferUpdateThread.start();
		
		this.latch.await(10, TimeUnit.SECONDS);
	}
	

	public void terminate() {
		
		this.running = false;		
	}


	public boolean isRunning() {
		
		return this.running;
	}
}
