package com.playground.photofacedetection.video

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.playground.photofacedetection.R
import com.playground.photofacedetection.video.frame.processor.FaceAlteringProcessor
import com.playground.photofacedetection.video.frame.processor.FaceContourOverlayDetectionProcessor
import com.playground.photofacedetection.video.frame.processor.FaceFastOverlayDetectionProcessor
import com.playground.photofacedetection.video.frame.processor.VisionImageProcessor
import com.playground.photofacedetection.common.CameraDirection
import com.playground.photofacedetection.common.Effect
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_realtime.*
import java.io.IOException

class RealTimeActivity : AppCompatActivity() {
    private lateinit var rxPermissions: RxPermissions
    private var disposable: Disposable? = null
    private var cameraSource: CameraSource = CameraSourceNoop
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)
        rxPermissions = RxPermissions(this)
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (rxPermissions.hasCameraPermissions) startCamera()
    }

    override fun onPause() {
        super.onPause()
        if (rxPermissions.hasCameraPermissions) cameraSourcePreviewView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
        disposable?.dispose()
    }

    private fun requestPermissions() {
        disposable?.dispose()
        disposable = rxPermissions.request(Manifest.permission.CAMERA)
            .subscribe { granted -> if (!granted) finish() }
    }

    private val RxPermissions.hasCameraPermissions get() = isGranted(Manifest.permission.CAMERA)
    private fun startCamera() {
        frontBackCameraButton.setOnClickListener {
            onCameraSwitched()
        }

        if (Camera.getNumberOfCameras() == 1) {
            frontBackCameraButton.visibility = View.GONE
        }

        cameraSource = CameraSourceImpl(this, graphicOverlayView)

        cameraSource.setFrameProcessor(processor)

        startCameraSource()
    }

    private val processor: VisionImageProcessor by lazy {
        val effect = intent?.extras?.getString(EFFECT_ARG)?.let {
            Effect.valueOf(it)
        } ?: Effect.BOX

        when (effect) {
            Effect.BLUR -> FaceAlteringProcessor(this)
            Effect.TROLL -> FaceFastOverlayDetectionProcessor(this, effect)
            Effect.BOX -> FaceFastOverlayDetectionProcessor(this, effect)
            Effect.OUTLINE -> FaceContourOverlayDetectionProcessor(effect)
        }
    }

    private fun onCameraSwitched() {
        val direction =
            if (graphicOverlayView.cameraDirection == CameraDirection.BACK) CameraDirection.FRONT
            else CameraDirection.BACK

        cameraSource.requestCameraDirection(direction)
        cameraSourcePreviewView.stop()
        startCameraSource()
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        try {
            cameraSourcePreviewView.start(cameraSource, graphicOverlayView)
        } catch (e: IOException) {
            Log.e(TAG, "Unable to start camera source. $e")
            cameraSource.release()
        }
    }

    companion object {
        private const val EFFECT_ARG = "EFFECT"
        fun start(context: Context, effect: Effect) =
            context.startActivity(Intent(context, RealTimeActivity::class.java).apply {
                putExtra(EFFECT_ARG, effect.name)
            })
    }
}

private const val TAG = "RealTimeActivity"

