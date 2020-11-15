package com.adigium.androidrfb

import android.R
import android.app.*
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import android.view.View
import androidx.core.app.NotificationCompat
import java.lang.ref.WeakReference
import java.util.*


class InAppInputManager(context: Context) :
    ActivityLifecycleCallbacks, Service() {
    private val currentActivityLock = Any() // Synchronize access to currentActivity and currentRootView
    private var currentActivity: WeakReference<Activity>? = null
    private var button1Pressed = false
    private var metaState = 0 // State of the meta keys

    private fun obtainTargetView(): View? {
        val a = obtainActivity() ?: return null
        val v = a.findViewById<View>(R.id.content).rootView ?: return null
        Log.i(TAG, "obtainTargetView vr:$a")
        if (v.hasWindowFocus()) return v // Quick way
        val lv = windowManagerViews
        for (vi in lv) {
            Log.i(TAG, "obtainTargetView vi:" + vi + " f:" + vi.hasWindowFocus())
            if (vi.hasWindowFocus()) return vi
        }
        return v
    }

    private fun obtainActivity(): Activity? {
        synchronized(currentActivityLock) {
            return if (currentActivity == null) null else currentActivity!!.get()
        }
    }

    fun onMouseEvent(buttonMask: Int, x: Int, y: Int): Boolean {
        if (!button1Pressed && buttonMask and 1 != 0) {
            injectTouchEvent(1, MotionEvent.ACTION_DOWN, x, y)
            button1Pressed = true
            return true
        } else if (button1Pressed) {
            if (buttonMask and 1 == 0) {
                injectTouchEvent(1, MotionEvent.ACTION_UP, x, y)
                button1Pressed = false
                return true
            } else {
                injectTouchEvent(1, MotionEvent.ACTION_MOVE, x, y)
                return true
            }
        }
        return false
    }

    private fun injectTouchEvent(buttonId: Int, event: Int, x: Int, y: Int) {
        Log.d("MouseController", "Starting InjectTouchEvent")
//        val view = obtainTargetView() ?: return
//        val activity = obtainActivity() ?: return
//        val viewLocation = IntArray(2)
//        view.getLocationOnScreen(viewLocation)
        val pointerProperties = PointerProperties()
        pointerProperties.toolType = MotionEvent.TOOL_TYPE_FINGER
        pointerProperties.id = 0
        val arrayOfPointerProperties = arrayOf(pointerProperties)
        val pc = PointerCoords()
        pc.size = 1f
        pc.pressure = 1f
        pc.x = x.toFloat() //- viewLocation[0].toFloat()
        pc.y = y.toFloat() //- viewLocation[1].toFloat()
//        Log.d("MouseController", "IntArray: [${viewLocation[0]}, ${viewLocation[1]}]")
        Log.d("MouseController", "pointerX: ${pc.x}, pointerY: ${pc.y}")
        val pcs = arrayOf(pc)
        val time = SystemClock.uptimeMillis()
        val eventToInject = MotionEvent.obtain(
                time,  // long downTime
                time + 100,  // long eventTime
                event,  // int action
                arrayOfPointerProperties.size,  // int pointerCount
                arrayOfPointerProperties,  // MotionEvent.PointerProperties[] pointerProperties
                pcs,  // MotionEvent.PointerCoords[] pointerCoords
                0,  // int metaState
                0, 1f, 1f,  // float yPrecision
                1,  // int deviceId
                0,  // int edgeFlags
                InputDevice.SOURCE_TOUCHSCREEN,  //int source
                0 // int flags
        )


        Log.d("MouseController", "Ending InjectTouchEvent")
        val instrumentation = Instrumentation()
        instrumentation.sendPointerSync(eventToInject)
//        InputManagerHelper.injectMotionEvent(eventToInject)
//        activity.runOnUiThread { view.dispatchTouchEvent(e) }
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

    override fun onCreate() {
        super.onCreate()
        startForeground(62318, builtNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun builtNotification(): Notification? {
        val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        var builder: NotificationCompat.Builder? = null
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel("ID", "Name", importance)
            // Creating an Audio Attribute
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(this, notificationChannel.id)
        } else {
            NotificationCompat.Builder(this)
        }
        builder.setDefaults(Notification.DEFAULT_LIGHTS)
        val message = "Forever running service"
        builder.setSmallIcon(R.drawable.ic_dialog_alert)
            .setAutoCancel(false)
            .setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(Color.parseColor("#0f9595"))
            .setContentTitle("Input events")
            .setContentText(message)
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val contentIntent = PendingIntent.getActivity(
                this, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(contentIntent)
        val notification: Notification = builder.build()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        return notification
    }
}