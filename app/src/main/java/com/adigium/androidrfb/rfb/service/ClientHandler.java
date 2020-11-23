package com.adigium.androidrfb.rfb.service;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


import com.adigium.androidrfb.rfb.screen.ScreenCapture;
//import com.adigium.androidrfb.RFB.keyboard.KeyboardController;
import com.adigium.androidrfb.rfb.mouse.MouseController;

class ClientHandler implements Runnable {

	private final Socket socket;
	private boolean running;
	private final RFBConfig config;
	private final MouseController mouseController;

	public ClientHandler(final Socket socket, final RFBConfig config) {
		this.socket = socket;
		this.running = false;
		this.config = config;
		this.mouseController = new MouseController();
	}
	

	public boolean isRunning() {
		return this.running;
	}
	

	public void terminate() {
		if (this.socket != null) {
			try {
				this.running = false;
				this.socket.close();
			} catch (final IOException exception) {
				Log.e("ClientHandler", "Client handler termination failure.", exception);
			}
		}
	}


	private short getWidth() {
		return (short) ScreenCapture.screenWidth;
	}
	private short getHeight() {
		return (short) ScreenCapture.screenHeight;
	}
	
	@Override
	public void run() {

		//
		// Some fixed values after handshake.
		//
		
		final ProtocolVersion ver;
		final SecurityTypes sec;
		final ClientInit clientInit;
		
		//
		// Variable values which VNC client might send a request to change.
		//
		
		SetPixelFormat setPixelFormat;
		SetEncodings setEncodings;
		
		//
		// Check & prepare TCP socket object.
		//

		if (this.socket == null) return;

		this.running = true;

		FramebufferUpdater frameBufferUpdater = null;

		try {

			final InputStream in   = this.socket.getInputStream();
			final OutputStream out = new BufferedOutputStream(this.socket.getOutputStream());

			// This is updater for frame buffer, running it its own thread.
			// Updater will receive frame buffer update requests from this thread,
			//  and it will write response message back to socket.
			frameBufferUpdater = new FramebufferUpdater(this, out);
			frameBufferUpdater.setPreferredEncodings(this.config.getPreferredEncodings()); // If set, favor encodings of RFB service, instead of VNC client encoding list.
			frameBufferUpdater.start();

			//
			// RFB protocol starts by sending version string
			//  and waiting for VNC client to reply with its version string.
			//

			ProtocolVersion.sendProtocolVersion(out);
			ver = ProtocolVersion.readProtocolVersion(in);
			Log.i("ClientHandler", "RFB protocol: " + ver);

			//
			// Send supported security types.
			//

			final byte[] securityTypes;

			if (this.config.getPassword() == null) {

				securityTypes = new byte[] {SecurityTypes.NONE};
			}
			else {

				securityTypes = new byte[] {SecurityTypes.VNC_AUTH};
			}

			SecurityTypes.send(out, securityTypes);
			sec = SecurityTypes.read(in);

			if (sec.securityType == SecurityTypes.VNC_AUTH) {

				// Send challenge data if VNC auth. is used.
				final VNCAuth vncAuth = new VNCAuth(this.config.getPassword());
				vncAuth.sendChallenge(out);
				vncAuth.readChallenge(in);

				if (vncAuth.isValid() == false) {

					// Wrong password received from VNC client!
					SecurityTypes.sendSecurityResult(out, "Wrong password.");

					this.running = false;

					this.socket.close();

					return;
				}
			}

			// SecurityResult message should be sent to VNC client.
			// 'The RFB Protocol' documentation, page 10.
			SecurityTypes.sendSecurityResult(out, null);

			// Wait for a ClientInit message.

			clientInit = ClientInit.readClientInit(in);

			if (!clientInit.sharedDesktop) {

				Log.i("ClientHandler","Client requested exclusive access to desktop. We won't kick other VNC clients for now.");
			}

			// ServerInit prepare and send.

			ServerInit.send(out, this.getWidth(), this.getHeight());

			// Run in loop, wait for some requests from client.

			while (this.running) {

				if (!frameBufferUpdater.isRunning()) break;

				final int EOF = -1
						, SET_PIXEL_FORMAT = 0
						, SET_ENCODINGS = 2
						, FRAMEBUFFER_UPDATE_REQUEST = 3
						, KEY_EVENT = 4
						, POINTER_EVENT = 5
						, CLIENT_CUT_TEXT = 6;

				// Read VNC client messages and handle them.

				int msgType = in.read();

				if (msgType == EOF) { break; }
				else if (msgType == SET_PIXEL_FORMAT) {
					in.read(new byte[3]); // padding.
					setPixelFormat = SetPixelFormat.read(in);
					Log.d("ClientHandler", "PixelFormat: "
							+ setPixelFormat.bitsPerPixel + " "
							+ setPixelFormat.depth + " "
							+ setPixelFormat.bigEndianFlag + " "
							+ setPixelFormat.trueColorFlag + " "
							+ setPixelFormat.redMax + " "
							+ setPixelFormat.greenMax + " "
							+ setPixelFormat.blueMax + " "
							+ setPixelFormat.redShift + " "
							+ setPixelFormat.greenShift + " "
							+ setPixelFormat.blueShift + " "
					);
					frameBufferUpdater.setPixelFormat(setPixelFormat);
				}
				else if (msgType == SET_ENCODINGS) {
					in.read(); // padding.
					setEncodings = SetEncodings.read(in);
					frameBufferUpdater.setClientEncodings(setEncodings);
				}
				else if (msgType == FRAMEBUFFER_UPDATE_REQUEST) {

					final FramebufferUpdateRequest request =
							FramebufferUpdateRequest.read(in);

					frameBufferUpdater.update(request);
				}
//				else if (msgType == KEY_EVENT) {
//
//					final KeyEvent keyEvent = KeyEvent.read(in);
//					KeyboardController.sendKey(keyEvent.key, keyEvent.downFlag);
//				}
				else if (msgType == POINTER_EVENT) {

					final PointerEvent pointerEvent = PointerEvent.read(in);

					int x = pointerEvent.xPos;
					int y = pointerEvent.yPos;

					this.mouseController.handleMouse(pointerEvent.buttonMask, x, y);

				}
				else if (msgType == CLIENT_CUT_TEXT) {
					//TODO: MAKE CLIPBOARD COPYING
//					final ClientCutText event = ClientCutText.read(in);
//
//					try {
//						final Clipboard clipboard =
//								Toolkit.getDefaultToolkit().getSystemClipboard();
//						final StringSelection selection = new StringSelection(event.text);
//						clipboard.setContents(selection, selection);
//					}
//					catch (final Exception ex) {
//						Log.e("ClientHandler", "Unable to copy to clipboard text.", ex);
//					}
				}
			}

			in.close();
			out.close();
		} catch (final IOException | InterruptedException exception) {
			if (this.running) {
				Log.i("ClientHandler", String.format("Client connection '%s' closed.", this.socket.getRemoteSocketAddress()));
			}
		}

		this.running = false;

		if (frameBufferUpdater != null) {
			frameBufferUpdater.terminate();
		}
	}

	@Override
	public String toString() {
		if (this.socket == null) {
			return ClientHandler.class.getSimpleName();
		}
		return String.format("%s-[%s]", ClientHandler.class.getSimpleName(), this.socket.getRemoteSocketAddress());
	}
}
