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
import com.playground.photofacedetection.video.GraphicOverlayView
import com.playground.photofacedetection.video.frame.graphic.DebugInfoGraphic
import com.playground.photofacedetection.video.frame.graphic.FaceBlurGraphic
import com.playground.photofacedetection.video.frame.graphic.BackgroundGraphic
import com.playground.photofacedetection.common.FrameMetadata
import java.io.IOException

class FaceAlteringProcessor(private val context: Context) :
    VisionProcessorBase<List<FirebaseVisionFace>>() {
    private val options = FirebaseVisionFaceDetectorOptions.Builder()
        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
        .build()
    private val detector: FirebaseVisionFaceDetector =
        FirebaseVision.getInstance().getVisionFaceDetector(options)

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
        graphicOverlayView.clear()
        if (originalCameraImage == null) return
        // if we got no results, just show the camera frame
        val graphic = if (results == null || results.isEmpty())
            BackgroundGraphic(graphicOverlayView, originalCameraImage)
        else
            FaceBlurGraphic(graphicOverlayView, results, context, originalCameraImage)
        graphicOverlayView.add(graphic)
        graphicOverlayView.add(
            DebugInfoGraphic(
                graphicOverlayView,
                droppedFrames.get()
            )
        )
        graphicOverlayView.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceAlteringProcessor"
    }
}