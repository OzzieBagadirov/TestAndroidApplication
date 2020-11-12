package com.adigium.androidrfb.RFB.mouse;

import com.adigium.androidrfb.InAppInputManager;


public class MouseController {
	public static InAppInputManager inputManager;
	public static float downscale;

	
	public MouseController() { }

	public void handleMouse(int buttonMask, int x, int y) {

		inputManager.onMouseEvent(buttonMask, Float.valueOf(x * downscale).intValue(), Float.valueOf(y * downscale).intValue());
	}
	
}
