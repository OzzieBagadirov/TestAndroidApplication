package com.adigium.androidrfb.helpers;

import android.hardware.input.InputManager;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.Method;

class InputManagerHelper {

    public static InputManager getInputManager() throws Exception {
        InputManager inputManager;
        Object[] objArr = new Object[0];
        inputManager = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0])
                .invoke(null, objArr);
        return inputManager;
    }

    public static Method getInjectInputEventMethod() throws Exception {
        return InputManager.class.getMethod(
                "injectInputEvent", new Class[] {InputEvent.class, Integer.TYPE});
    }

    public static void injectMotionEvent(MotionEvent event) throws Exception{
        getInjectInputEventMethod().invoke(getInputManager(), new Object[]{event, Integer.valueOf(0)});
    }

}
