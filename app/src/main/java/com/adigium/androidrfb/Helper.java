package com.adigium.androidrfb;

import android.app.Instrumentation;
import android.view.MotionEvent;

public class Helper {
    public void sendPointerSync(MotionEvent event) {
        Instrumentation instrumentation = new Instrumentation();
        instrumentation.sendPointerSync(event);
    }
}
