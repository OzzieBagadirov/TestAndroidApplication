package com.adigium.androidrfb.RFB.screen;

import android.media.Image;
import android.media.ImageReader;

import com.adigium.androidrfb.InAppInputManager;
import com.adigium.androidrfb.RFB.image.TrueColorImage;

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
		//TODO: RECEIVING IMAGE FROM ANDROID DEVICE
		Image image = imageReader.acquireLatestImage();
		int[] colorImageBuffer = image.getPlanes()[0].getBuffer().asIntBuffer().array();

	    return new TrueColorImage(colorImageBuffer, image.getWidth(), image.getHeight());
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
