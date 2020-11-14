package com.adigium.androidrfb.RFB.mouse;

import android.util.Log;

import com.adigium.androidrfb.InAppInputManager;


public class MouseController {
	public static InAppInputManager inputManager;
	public static float downscale;

	
	public MouseController() { }

	public void handleMouse(int buttonMask, int x, int y) {
		Log.d("MouseController", "x = " + x + "; y = " + y);
		inputManager.onMouseEvent(buttonMask, Float.valueOf(x * downscale).intValue(), Float.valueOf(y * downscale).intValue());
	}
	
}
