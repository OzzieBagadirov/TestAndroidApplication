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
		Image image = null;
		int[] arrayBuffer = null;
		int width = 0;
		int height = 0;
		try {
			if (FramebufferUpdater.imageReady - 1 >= 0) {
				image = imageReader.acquireLatestImage();
				FramebufferUpdater.imageReady -= 1;
				Log.d("ImageReader", "Image taken. Images now: " + FramebufferUpdater.imageReady);
				if (image != null) {
					width = image.getWidth();
					height = image.getHeight();
					IntBuffer colorImageBuffer = image.getPlanes()[0].getBuffer().asIntBuffer();

					try {
						image.close();
					} catch (IllegalStateException e) {
						Log.w("ScreenCapture", "Image already closed");
					}

					arrayBuffer = new int[colorImageBuffer.limit()];
					colorImageBuffer.get(arrayBuffer);
				} else {
					FramebufferUpdater.imageReady = 0;
					return null;
				}
			} else return null;
		} catch (Exception e) {
			Log.e("ScreenCapture", "Error: ", e);
		}

		return new TrueColorImage(arrayBuffer, width, height);
	}
}
