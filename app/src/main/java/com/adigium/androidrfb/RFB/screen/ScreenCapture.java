package com.adigium.androidrfb.RFB.screen;

import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import com.adigium.androidrfb.InAppInputManager;
import com.adigium.androidrfb.RFB.image.TrueColorImage;
import com.adigium.androidrfb.RFB.service.FramebufferUpdater;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Java routines to capture current screen.
 * <p>
 * Note that on 32-bit true color systems,
 * image of screen is usually in the following byte order:
 * <pre>
 * [A R G B]
 * </pre>
 * while VNC clients might expect following byte order:
 * <pre>
 * [B G R 0]
 * </pre>
 * <p>
 * 
 * @author igor.delac@gmail.com
 *
 */
public class ScreenCapture {
	public static int screenWidth;
	public static int screenHeight;
	public static ImageReader imageReader;

	public static TrueColorImage getScreenshot() {

//		final Robot robot = new Robot();
//
//		final Rectangle screenRect = new Rectangle(x, y, width, height);
//		final BufferedImage colorImage = robot.createScreenCapture(screenRect);
		Image image = null;
		int[] arrayBuffer = null;
		int width = 0;
		int height = 0;
		try {
			if (FramebufferUpdater.imageReady - 1 >= 0) {
				image = imageReader.acquireLatestImage();
				if (image != null) {
					width = image.getWidth();
					height = image.getHeight();
					IntBuffer colorImageBuffer = image.getPlanes()[0].getBuffer().asIntBuffer();

					try {
						image.close();
					} catch (IllegalStateException e) {
						Log.w("ScreenCapture", "Image already closed");
					}
					Log.d("ImageReader", "Image taken. Images now: " + FramebufferUpdater.imageReady);
					FramebufferUpdater.imageReady -= 1;

					arrayBuffer = new int[colorImageBuffer.limit()];
					colorImageBuffer.get(arrayBuffer);

					Log.d("ScreenCapture", Arrays.toString(arrayBuffer));
				} else return null;
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e("ScreenCapture", "Error: ", e);
		}

		return new TrueColorImage(arrayBuffer, width, height);
	}
	
	/**
	 * This method will fill image buffer with complete screen (primary screen).
	 * Buffer is always filled with ARGB values (32-bit).
	 * 
	 * @return	{@link TrueColorImage} which contains array of int's representing pixels of current screen
	 * 
	 * @throws AWTException	if running in headless mode, eg. without X11, or any display 
	 */
//	public static TrueColorImage getScreenshot() throws AWTException {
//		return getScreenshot();
//	}
}
