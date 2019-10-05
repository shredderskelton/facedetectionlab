package com.playground.photofacedetection.video.frame.processor

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.FAST
import com.playground.photofacedetection.common.FrameMetadata
import com.playground.photofacedetection.video.GraphicOverlayView
import com.playground.photofacedetection.video.frame.graphic.BackgroundGraphic
import com.playground.photofacedetection.video.frame.graphic.DebugInfoGraphic
import com.playground.photofacedetection.video.frame.graphic.FaceOutlineGraphic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface PhotoProducer {
    var activated: Boolean
}

interface DetectionWatcher {
    fun onDetectionAvailabilityChanged(available: Boolean)
    fun onFaceDetected()
}

class FaceSaveDetectionProcessor(
    private val context: Context,
    private val watcher: DetectionWatcher
) :
    VisionProcessorBase<List<FirebaseVisionFace>>(), PhotoProducer {

    private var count = 0

    override var activated: Boolean = false

    private val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
        .setPerformanceMode(FAST)
        .build()
    private val detector: FirebaseVisionFaceDetector =
        FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts)

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionFace>?,
        frameMetadata: FrameMetadata,
        graphicOverlayView: GraphicOverlayView
    ) {
        if (activated && originalCameraImage != null && results?.size == 1) {
            saveFace(originalCameraImage, results.first())
        }

        graphicOverlayView.clear()
        if (originalCameraImage == null) return
        // original camera frame
        graphicOverlayView.add(
            BackgroundGraphic(graphicOverlayView, originalCameraImage)
        )
        // effect overlay
        results?.forEach { face ->
            val faceGraphic = FaceOutlineGraphic(graphicOverlayView, face)
            graphicOverlayView.add(faceGraphic)
        }
        // debug overlay
        graphicOverlayView.add(
            DebugInfoGraphic(graphicOverlayView, droppedFrames.get())
        )
        graphicOverlayView.postInvalidate()
    }

    private fun saveFace(bitmap: Bitmap, face: FirebaseVisionFace) {
        val faceBitmap: Bitmap = Bitmap.createBitmap(
            bitmap,
            face.boundingBox.left,
            face.boundingBox.bottom,
            face.boundingBox.width(),
            face.boundingBox.height()
        )

        val task = SaveTask(context, "${count++}", faceBitmap)
        GlobalScope.launch {
            task.save()
            println("saved")
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceFastOverlay"
    }
}

class SaveTask(
    private val context: Context,
    private val uniqueId: String,
    private val bmp: Bitmap
) {
    fun save() {
        val dir: File = context.getExternalFilesDir("photos/")!!

        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "nick_$uniqueId.jpg")
        val fOut = FileOutputStream(file)

        bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
        fOut.flush()
        fOut.close()
    }
}