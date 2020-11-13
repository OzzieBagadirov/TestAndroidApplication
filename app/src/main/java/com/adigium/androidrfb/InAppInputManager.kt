package com.adigium.androidrfb

import android.R
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import android.view.View
import java.lang.ref.WeakReference
import java.util.*

class InAppInputManager(context: Context?) :
    ActivityLifecycleCallbacks {
    private val currentActivityLock = Any() // Synchronize access to currentActivity and currentRootView
    private var currentActivity: WeakReference<Activity>? = null
    private var button1Pressed = false
    private var metaState = 0 // State of the meta keys

    private fun obtainTargetView(): View? {
        val a = obtainActivity() ?: return null
        val v = a.findViewById<View>(R.id.content).rootView ?: return null
        if (v.hasWindowFocus()) return v // Quick way
        val lv = windowManagerViews
        for (vi in lv) {
            if (vi.hasWindowFocus()) return vi
        }
        return v
    }

    private fun obtainActivity(): Activity? {
        synchronized(currentActivityLock) {
            return if (currentActivity == null) null else currentActivity!!.get()
        }
    }

    fun onMouseEvent(buttonMask: Int, x: Int, y: Int) {
        if (!button1Pressed && buttonMask and 1 != 0) {
            injectTouchEvent(1, MotionEvent.ACTION_DOWN, x, y)
//            Log.d("InputManager", "Button 1 Pressed")
            button1Pressed = true
        } else if (button1Pressed) {
            if (buttonMask and 1 == 0) {
                injectTouchEvent(1, MotionEvent.ACTION_UP, x, y)
//                Log.d("InputManager", "Button 1 Released")
                button1Pressed = false
            } else {
                injectTouchEvent(1, MotionEvent.ACTION_MOVE, x, y)
//                Log.d("InputManager", "Button 1 Moved")
            }

        }
    }

    private fun injectTouchEvent(buttonId: Int, event: Int, x: Int, y: Int) {
        val view = obtainTargetView() ?: return
        val activity = obtainActivity() ?: return
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val pp = PointerProperties()
        pp.toolType = MotionEvent.TOOL_TYPE_FINGER
        pp.id = 0
        val pps = arrayOf(pp)
        val pc = PointerCoords()
        pc.size = 1f
        pc.pressure = 1f
        pc.x = x - viewLocation[0].toFloat()
        pc.y = y - viewLocation[1].toFloat()
        val pcs = arrayOf(pc)
        val t = SystemClock.uptimeMillis()
        val e = MotionEvent.obtain(
            t,  // long downTime
            t + 100,  // long eventTime
            event,  // int action
            pps.size,  // int pointerCount
            pps,  // MotionEvent.PointerProperties[] pointerProperties
            pcs,  // MotionEvent.PointerCoords[] pointerCoords
            0,  // int metaState
            0, 1f, 1f,  // float yPrecision
            1,  // int deviceId
            0,  // int edgeFlags
            InputDevice.SOURCE_TOUCHSCREEN,  //int source
            0 // int flags
        )
        activity.runOnUiThread { view.dispatchTouchEvent(e) }
    }

    private fun injectBackEvent() {
        val activity = obtainActivity() ?: return
        activity.runOnUiThread { activity.onBackPressed() }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        Log.i(TAG, "onActivityStarted:$activity")
        synchronized(currentActivityLock) {
            currentActivity = WeakReference(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Log.i(TAG, "onActivityResumed:$activity")
        synchronized(currentActivityLock) {
            currentActivity = WeakReference(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.i(TAG, "onActivityPaused:$activity")
        synchronized(currentActivityLock) {
            if (currentActivity != null && currentActivity!!.get() === activity) {
                currentActivity = null
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        Log.i(TAG, "onActivityStopped:$activity")
        synchronized(currentActivityLock) {
            if (currentActivity != null && currentActivity!!.get() === activity) {
                currentActivity = null
            }
        }
    }

    ///
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private val TAG =
            InAppInputManager::class.java.name// get the list from WindowManagerGlobal.mViews

        // get the list from WindowManagerImpl.mViews
        private val windowManagerViews: List<View>
            private get() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1
                    ) {

                        // get the list from WindowManagerImpl.mViews
                        val wmiClass = Class.forName("android.view.WindowManagerImpl")
                        val wmiInstance = wmiClass.getMethod("getDefault").invoke(null)
                        return viewsFromWM(wmiClass, wmiInstance)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                        // get the list from WindowManagerGlobal.mViews
                        val wmgClass = Class.forName("android.view.WindowManagerGlobal")
                        val wmgInstance = wmgClass.getMethod("getInstance").invoke(null)
                        return viewsFromWM(wmgClass, wmgInstance)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return ArrayList()
            }

        @Throws(Exception::class)
        private fun viewsFromWM(wmClass: Class<*>, wmInstance: Any): List<View> {
            val viewsField = wmClass.getDeclaredField("mViews")
            viewsField.isAccessible = true
            val views = viewsField[wmInstance]
            if (views is List<*>) {
                return viewsField[wmInstance] as List<View>
            } else if (views is Array<*>) {
                return listOf(*viewsField[wmInstance] as Array<*>) as List<View>
            }
            return ArrayList()
        }
    }

    init {
        var app: Application? = null
        app = try {
            context as Application?
        } catch (e: ClassCastException) {
            null
        }
        if (app == null) {
            Log.e(TAG, "Provided context is not an application - input events will not work")
        }
        app!!.registerActivityLifecycleCallbacks(this)
    }
}