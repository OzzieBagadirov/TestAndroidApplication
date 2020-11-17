package com.adigium.androidrfb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.core.content.ContextCompat.getSystemService
import com.adigium.androidrfb.rfb.encoding.RichCursorEncoder
import com.adigium.androidrfb.rfb.mouse.MouseController
import com.adigium.androidrfb.rfb.screen.ScreenCapture
import com.adigium.androidrfb.rfb.service.FramebufferUpdater
import com.adigium.androidrfb.rfb.service.RFBService

class RemoteControl() {
    private val TAG = "REMOTE_CONTROL"
    private var context: Context? = null
    private var downscale = 0.0

    private var host: String = "localhost"
    private var port: Int = 60000

    private var projectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var captureThread: Thread? = null

    private var imageReader: ImageReader? = null
    private var handler: Handler? = null

    private var virtualDisplay: VirtualDisplay? = null

    private var density = 0
    private var display: Display? = null

    private var rfbService: RFBService? = null
    private var inputManager: InAppInputManager? = null

    private var mediaProjectionPixelFormat = PixelFormat.RGBA_8888

    private val minAPILevel = Build.VERSION_CODES.LOLLIPOP

    constructor(context: Context, host: String, port: Int): this() {

        this.context = context
        rfbService = RFBService(host, port)
        this.host = host
        this.port = port
        inputManager = InAppInputManager(context)
        MouseController.inputManager = inputManager
        RichCursorEncoder.context = context
    }

    fun start(activity: Activity, requestCode: Int) {

        // call for the projection manager
        projectionManager = getSystemService(context!!, MediaProjectionManager::class.java) as MediaProjectionManager

        if (captureThread == null) {
            // run capture handling thread
            captureThread = Thread {
                Looper.prepare()
                handler = Handler()
                Looper.loop()
            }
            captureThread!!.start()
        }
        activity.startActivityForResult(
            projectionManager!!.createScreenCaptureIntent(),
            requestCode
        )
    }

    fun stop() {
        if (captureThread == null) return
        if (handler == null) return
        handler!!.post {
            if (Build.VERSION.SDK_INT < minAPILevel) {
                Log.w(
                    TAG,
                    "Can't run shutdown() due to a low API level. API level 21 or higher is required."
                )
            } else {
                if (mediaProjection != null) {
                    mediaProjection!!.stop()
                    mediaProjection = null
                    try {
                        handler!!.post{Log.d(TAG, "Stop handler post")}
                    } catch (e: Exception) {

                    }
                }
            }
//            this@CatVision.sendBroadcast(CVIOInternals.createIntent(com.teskalabs.cvio.CatVision.ACTION_CVIO_SHARE_STOPPED))
        }
        rfbService!!.terminate()
    }

    fun onActivityResult(activity: Activity, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(data!!)
        } else {
//            activity.startService(data!!)
        }
        mediaProjection = projectionManager!!.getMediaProjection(resultCode, data!!)
        if (mediaProjection != null) {
            // display metrics
            val metrics: DisplayMetrics = context!!.resources.displayMetrics
            density = metrics.densityDpi
            display = activity.windowManager.defaultDisplay
            if (downscale == 0.0) {
                downscale = if (density < 280) {
                    1.0
                } else if (density < 400) {
                    2.0
                } else {
                    3.0
                }
            }
            MouseController.downscale = downscale.toFloat()

            // create virtual display depending on device width / height
            createVirtualDisplay()

            // register media projection shutdown callback
            mediaProjection!!.registerCallback(MediaProjectionStopCallback(), handler)
    //                this.sendBroadcast(CVIOInternals.createIntent(com.teskalabs.cvio.CatVision.ACTION_CVIO_SHARE_STARTED))
        }
    }

    inner class ImageAvailableListener : OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            FramebufferUpdater.imageReady()
//            Log.d("ImageReader", "New image available. Images now: " + FramebufferUpdater.imageReady)
        }
    }

    private fun setImageReader() {
        //TODO: Consider synchronisation
        if (imageReader == null) return
        ScreenCapture.imageReader = imageReader
    }

    inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            handler!!.post {
                if (virtualDisplay != null) virtualDisplay!!.release()
                if (imageReader != null) imageReader!!.setOnImageAvailableListener(null, null)
                if (mediaProjection != null) mediaProjection!!.unregisterCallback(
                    this@MediaProjectionStopCallback
                )
                mediaProjection = null
                rfbService!!.terminate()
            }
        }
    }

    private fun createVirtualDisplay() {
        if (Build.VERSION.SDK_INT < minAPILevel) {
            Log.w(
                TAG,
                "Can't run createVirtualDisplay() due to a low API level. API level 21 or higher is required."
            )
            return
        }
        // get width and height
        val size = Point()
        display!!.getRealSize(size)
        val width = (size.x / downscale).toInt()
        val height = (size.y / downscale).toInt()
        if (rfbService != null) {
            rfbService = RFBService(host, port)
            rfbService!!.start(width, height)
        } else {
            rfbService = RFBService(host, port)
            rfbService!!.start(width, height)
        }
        val VIRTUAL_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC

        // run capture reader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        setImageReader()
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "cvio",
            width,
            height,
            density,
            VIRTUAL_DISPLAY_FLAGS,
            imageReader!!.surface,
            null,
            handler
        )

        imageReader!!.setOnImageAvailableListener(ImageAvailableListener(), handler)
    }


//    override fun rfbKbdAddEventProc(down: Boolean, keySymCode: Long, client: String?) {
////        val ks: KeySym = KeySym.lookup.get(keySymCode.toInt())
////        inputManager.onKeyboardEvent(down, ks)
//    }
//
//    override fun rfbKbdReleaseAllKeysProc(client: String) {
////        Log.d(com.teskalabs.cvio.CatVision.TAG, "rfbKbdReleaseAllKeysProc: client:$client")
//    }
//
//    override fun rfbPtrAddEventProc(buttonMask: Int, x: Int, y: Int, client: String?) {
//        inputManager!!.onMouseEvent(buttonMask, (x * downscale).toInt(), (y * downscale).toInt())
//    }
//
//    override fun rfbSetXCutTextProc(text: String, client: String) {
//        Log.d(
//            TAG, "rfbSetXCutTextProc: text:$text client:$client"
//        )
//    }
//
//    override fun rfbNewClientHook(client: String?): Int {
//        try {
//            handler!!.post { Log.d(TAG, "rfbNewClientHook") }
//        } catch (e: java.lang.Exception) {
//            Log.e(TAG, "Failed to trigger SeaCat event", e)
//        }
//        return 0
//    }
}