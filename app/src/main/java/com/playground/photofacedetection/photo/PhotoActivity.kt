package com.playground.photofacedetection.photo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.playground.photofacedetection.R
import com.playground.photofacedetection.common.CameraDirection
import com.playground.photofacedetection.common.Effect
import com.playground.photofacedetection.photo.detector.FaceDetector
import com.playground.photofacedetection.photo.detector.SimpleFaceDetector
import com.playground.photofacedetection.photo.detector.SmileyFaceDetector
import com.tbruyelle.rxpermissions2.RxPermissions
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.result.BitmapPhoto
import io.fotoapparat.selector.auto
import io.fotoapparat.selector.autoFocus
import io.fotoapparat.selector.back
import io.fotoapparat.selector.continuousFocusPicture
import io.fotoapparat.selector.firstAvailable
import io.fotoapparat.selector.fixed
import io.fotoapparat.selector.front
import io.fotoapparat.selector.highestFps
import io.fotoapparat.selector.highestResolution
import io.fotoapparat.selector.hz50
import io.fotoapparat.selector.hz60
import io.fotoapparat.selector.manualJpegQuality
import io.fotoapparat.selector.none
import io.fotoapparat.selector.off
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var rxPermissions: RxPermissions
    private lateinit var disposable: Disposable
    private lateinit var foto: Fotoapparat
    private var cameraDirection = CameraDirection.FRONT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rxPermissions = RxPermissions(this)
        foto = Fotoapparat(
            context = this,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = front(),
            cameraConfiguration = cameraConfiguration
        )
        requestPermissions()
    }

    private val detection: FaceDetector by lazy {
        val effect = intent?.extras?.getString(EFFECT_ARG)?.let {
            Effect.valueOf(it)
        } ?: Effect.BOX

        when (effect) {
            Effect.BLUR,
            Effect.TROLL,
            Effect.BOX -> SimpleFaceDetector(
                this,
                effect
            )
            Effect.OUTLINE -> SmileyFaceDetector()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rxPermissions.hasCameraPermissions) startCamera()
    }

    override fun onPause() {
        super.onPause()
        if (rxPermissions.hasCameraPermissions) stopCamera()
    }

    private fun requestPermissions() {
        disposable = rxPermissions.request(Manifest.permission.CAMERA)
            .subscribe { granted -> if (!granted) finish() }
    }

    private fun startCamera() {
        foto.start()
        shootButton.setOnClickListener {
            foto.takePicture()
                .toBitmap()
                .transform { it.rotate() }
                .whenAvailable { photo ->
                    photo?.let { bitmap ->
                        detection.process(bitmap, ::onProcessed, cameraDirection)
                    }
                }
        }
        changeCameraButton.setOnClickListener {
            val camera = if (cameraDirection == CameraDirection.FRONT) back() else front()
            foto.switchTo(camera, cameraConfiguration)
            cameraDirection = when (cameraDirection) {
                CameraDirection.FRONT -> CameraDirection.BACK
                CameraDirection.BACK -> CameraDirection.FRONT
            }
        }
        tryAgainButton.setOnClickListener {
            resultsGroup.visibility = View.GONE
        }
    }

    private fun BitmapPhoto.rotate(): Bitmap {
        val rotationCompensation = -rotationDegrees.toFloat()
        val source = bitmap
        val matrix = Matrix()
        if (cameraDirection == CameraDirection.FRONT)
            matrix.preScale(1f, -1f) // mirror front camera
        matrix.postRotate(rotationCompensation)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun onProcessed(bitmapWithFaces: Bitmap) {
        resultView.setImageBitmap(bitmapWithFaces)
        resultsGroup.visibility = View.VISIBLE
    }

    private fun stopCamera() {
        foto.stop()
    }

    private val RxPermissions.hasCameraPermissions get() = isGranted(Manifest.permission.CAMERA)

    companion object {
        private const val EFFECT_ARG = "EFFECT_PHOTO"
        fun start(context: Context, effect: Effect) =
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                putExtra(EFFECT_ARG, effect.name)
            })
    }
}

private const val TAG = "MainActivity"
private val cameraConfiguration = CameraConfiguration(
    pictureResolution = {
        filter { it.width in 500..800 }.minBy { it.area } // select custom resolution from available
    },
    previewResolution = highestResolution(), // (optional) we want to have the highest possible preview resolution
    previewFpsRange = highestFps(),          // (optional) we want to have the best frame rate
    focusMode = firstAvailable(              // (optional) use the first focus mode which is supported by device
        continuousFocusPicture(),
        autoFocus(),                       // if continuous focus is not available on device, auto focus will be used
        fixed()                            // if even auto focus is not available - fixed focus mode will be used
    ),
    flashMode = firstAvailable(              // (optional) similar to how it is done for focus mode, this time for flash
//        autoRedEye(),
//        autoFlash(),
//        torch(),
        off()
    ),
    antiBandingMode = firstAvailable(       // (optional) similar to how it is done for focus mode & flash, now for anti banding
        auto(),
        hz50(),
        hz60(),
        none()
    ),
    jpegQuality = manualJpegQuality(90)
)

