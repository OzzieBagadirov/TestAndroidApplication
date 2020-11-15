package com.adigium.androidrfb

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import androidx.core.app.NotificationCompat


class InAppInputManager(context: Context) : Service() {
    private var button1Pressed = false
    private var metaState = 0 // State of the meta keys
    private var instrumentation: Instrumentation? = null

    private val moveEventFrequency = 3 //TODO: MAKE MOVE EVENT FREQUENCY DEPENDING ON DPI
    private var moveEventCounter = 0

    init {
        instrumentation = Instrumentation()
    }

    fun onMouseEvent(buttonMask: Int, x: Int, y: Int) {
        if (!button1Pressed && buttonMask and 1 != 0) {
            injectTouchEvent(1, MotionEvent.ACTION_DOWN, x, y)
            button1Pressed = true
        } else if (button1Pressed) {
            if (buttonMask and 1 == 0) {
                injectTouchEvent(1, MotionEvent.ACTION_UP, x, y)
                button1Pressed = false
            } else {
                when {
                    moveEventCounter == 0 -> {
                        injectTouchEvent(1, MotionEvent.ACTION_MOVE, x, y)
                        moveEventCounter += 1
                    }
                    moveEventCounter + 1 == moveEventFrequency -> {
                        moveEventCounter = 0
                    }
                    else -> {
                        moveEventCounter += 1
                    }
                }
            }
        }
    }

    private fun injectTouchEvent(buttonId: Int, event: Int, x: Int, y: Int) {
        Log.d("MouseController", "Starting InjectTouchEvent")
        val pointerProperties = PointerProperties()
        pointerProperties.toolType = MotionEvent.TOOL_TYPE_FINGER
        pointerProperties.id = 0
        val arrayOfPointerProperties = arrayOf(pointerProperties)
        val pc = PointerCoords()
        pc.size = 1f
        pc.pressure = 1f
        pc.x = x.toFloat() //- viewLocation[0].toFloat()
        pc.y = y.toFloat() //- viewLocation[1].toFloat()
        val pcs = arrayOf(pc)
        val time = SystemClock.uptimeMillis()

        val eventToInject = MotionEvent.obtain(
                time,                           // long downTime
                time,                           // long eventTime
                event,                          // int action
                arrayOfPointerProperties.size,  // int pointerCount
                arrayOfPointerProperties,       // MotionEvent.PointerProperties[] pointerProperties
                pcs,                            // MotionEvent.PointerCoords[] pointerCoords
                0,                      // int metaState
                0,                     // int buttonState
                1f, 1f,        // float precision
                1,                       // int deviceId
                0,                      // int edgeFlags
                InputDevice.SOURCE_TOUCHSCREEN, //int source
                0                           // int flags
        )


        Log.d("MouseController", "Ending InjectTouchEvent")
        instrumentation!!.sendPointerSync(eventToInject)
//        InputManagerHelper.injectMotionEvent(eventToInject)
    }

    companion object {
        private val TAG = InAppInputManager::class.java.name
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