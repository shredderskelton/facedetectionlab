package com.playground.photofacedetection.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import java.io.IOException

/** Preview the camera image in the screen.  */
@Suppress("DEPRECATION")
@SuppressLint("MissingPermission", "LogNotTimber")
class CameraSourcePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {
    private val surfaceView: SurfaceView
    private var startRequested: Boolean = false
    private var surfaceAvailable: Boolean = false
    private var cameraSource: CameraSource =
        CameraSourceNoop
    private lateinit var overlayView: GraphicOverlayView
    private val isPortraitMode: Boolean
        get() {
            val orientation = context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }

            Log.d(TAG, "isPortraitMode returning false by default")
            return false
        }

    init {
        startRequested = false
        surfaceAvailable = false

        surfaceView = SurfaceView(context)
        surfaceView.holder.addCallback(SurfaceCallback())
        addView(surfaceView)
    }

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource) {
        this.cameraSource = cameraSource
        startRequested = true
        startIfReady()
    }

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource, overlayView: GraphicOverlayView) {
        this.overlayView = overlayView
        start(cameraSource)
    }

    fun stop() = cameraSource.stop()
    fun release() = cameraSource.release()
    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun startIfReady() {
        if (startRequested && surfaceAvailable) {
            cameraSource.start()
            val size = cameraSource.cameraPreviewSize
            val min = Math.min(size!!.width, size.height)
            val max = Math.max(size.width, size.height)
            // Swap width and height sizes when in portrait, since it will be rotated by
            // 90 degrees
            if (isPortraitMode) overlayView.setCameraInfo(min, max, cameraSource.activeCamera)
            else overlayView.setCameraInfo(max, min, cameraSource.activeCamera)
            overlayView.clear()
        } else {
            Log.i(
                TAG,
                "Not ready: startRequested:$startRequested surfaceAvailable:$surfaceAvailable"
            )
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            surfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            surfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // These values should be as close to the the dimensions of your preview window as possible
        // TODO compute the magic numbers instead of hardcoding them to Pixel 3 values!
        var width = 603 // (2160px / 3) - StatusBar - Nav bar  - buttons
        var height = 360 // (1080px / 3)
        val size = cameraSource.cameraPreviewSize
        if (size != null) {
            width = size.width
            height = size.height
        }
        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = width
            width = height
            height = tmp
        }
        Log.i(TAG, "Size is $width, $height")
        val layoutWidth = right - left
        val layoutHeight = bottom - top
        // Computes height and width for potentially doing fit width.
        var childWidth = layoutWidth
        var childHeight = (layoutWidth.toFloat() / width.toFloat() * height).toInt()
        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight
            childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
        }

        for (i in 0 until childCount) {
            getChildAt(i).layout(0, 0, childWidth, childHeight)
            Log.d(TAG, "Assigned view: $i")
        }

        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    companion object {
        private const val TAG = "CameraSourcePreviewView"
    }
}
