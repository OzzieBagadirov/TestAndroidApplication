package com.adigium.androidrfb.rfb.screen;

import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import com.adigium.androidrfb.rfb.image.TrueColorImage;
import com.adigium.androidrfb.rfb.service.FramebufferUpdater;

import java.nio.IntBuffer;

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
 */
public class ScreenCapture {
	public static int screenWidth;
	public static int screenHeight;
	public static ImageReader imageReader;

	public static TrueColorImage getScreenshot() {
		Image image = null;
		int[] arrayBuffer = null;
		int width = 0;
		int height = 0;
		try {
			if (FramebufferUpdater.imageReady != 0) {
				FramebufferUpdater.imageReady = 0;
				image = imageReader.acquireLatestImage();

//				Log.d("ScreenCapture","Image Format: " + image.getFormat());
//				Log.d("ImageReader", "Image taken. Images now: " + FramebufferUpdater.imageReady);
				if (image != null) {
					width = image.getWidth();
					height = image.getHeight();
					IntBuffer colorImageBuffer = image.getPlanes()[0].getBuffer().asIntBuffer();

					arrayBuffer = new int[colorImageBuffer.limit()];
					colorImageBuffer.get(arrayBuffer);

					try {
						image.close();
					} catch (IllegalStateException e) {
						Log.w("ScreenCapture", "Image already closed");
					}
				} else {
					FramebufferUpdater.imageReady = 0;
					return null;
				}
			} else return null;
		} catch (Exception e) {
			Log.e("ScreenCapture", "Error: ", e);
		}

		//TODO: MAKE BYTE ORDER TEMPLATES AND SYNC WITH CLIENT
		return new TrueColorImage(arrayBuffer, width, height).toBGR();
	}
}
