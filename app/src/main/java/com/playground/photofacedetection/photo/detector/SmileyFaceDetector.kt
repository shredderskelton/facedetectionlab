package com.playground.photofacedetection.photo.detector

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.FAST
import com.playground.photofacedetection.common.CameraDirection
import com.playground.photofacedetection.photo.graphic.SmileyFaceGraphic

class SmileyFaceDetector : FaceDetector {
    private val options =
        FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FAST)
            .setContourMode(ALL_LANDMARKS)
            .build()
    private val detector: FirebaseVisionFaceDetector =
        FirebaseVision.getInstance().getVisionFaceDetector(options)

    override fun process(
        bitmap: Bitmap,
        callback: (Bitmap) -> Unit,
        cameraDirection: CameraDirection
    ) {
        val fireImage = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(fireImage)
            .addOnSuccessListener { faces ->
                val bitmapWithFaces = SmileyFaceGraphic()
                    .draw(bitmap, faces)
                callback(bitmapWithFaces)
            }
            .addOnFailureListener { e -> Log.e(TAG, "$e") }
    }
}

private const val TAG = "SmileyFaceDetector"