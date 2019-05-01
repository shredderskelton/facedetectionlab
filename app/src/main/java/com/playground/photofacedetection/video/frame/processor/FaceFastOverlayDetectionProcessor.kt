package com.playground.photofacedetection.video.frame.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.FAST
import com.playground.photofacedetection.common.Effect
import com.playground.photofacedetection.R
import com.playground.photofacedetection.video.GraphicOverlayView
import com.playground.photofacedetection.video.frame.graphic.DebugInfoGraphic
import com.playground.photofacedetection.video.frame.graphic.FaceContoursGraphic
import com.playground.photofacedetection.video.frame.graphic.FaceOutlineGraphic
import com.playground.photofacedetection.video.frame.graphic.BackgroundGraphic
import com.playground.photofacedetection.video.frame.graphic.FaceOverlayGraphic
import com.playground.photofacedetection.common.FrameMetadata
import java.io.IOException

class FaceFastOverlayDetectionProcessor(context: Context, private val effect: Effect) :
    VisionProcessorBase<List<FirebaseVisionFace>>() {
    private val trollBitmap = BitmapFactory.decodeResource(context.resources, R.raw.troll)
    // Real-time contour detection of multiple faces
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
        graphicOverlayView.clear()
        if (originalCameraImage == null) return
        // original camera frame
        graphicOverlayView.add(
            BackgroundGraphic(graphicOverlayView, originalCameraImage)
        )
        // effect overlay
        results?.forEach { face ->
            val faceGraphic =
                when (effect) {
                    Effect.BLUR -> TODO()
                    Effect.TROLL -> FaceOverlayGraphic(
                        graphicOverlayView,
                        face,
                        trollBitmap
                    )
                    Effect.BOX -> FaceOutlineGraphic(graphicOverlayView, face)
                    Effect.OUTLINE -> FaceContoursGraphic(graphicOverlayView, face)
                }

            graphicOverlayView.add(faceGraphic)
        }
        // debug overlay
        graphicOverlayView.add(
            DebugInfoGraphic(graphicOverlayView, droppedFrames.get())
        )
        graphicOverlayView.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceFastOverlay"
    }
}