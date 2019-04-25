package com.playground.photofacedetection.detector

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.*
import com.playground.photofacedetection.graphic.SmileyFaceGraphic

class SmileyFaceDetector : FaceDetector {
    private val options =
        FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FAST)
            .setContourMode(ALL_LANDMARKS)
            .build()
    private val detector: FirebaseVisionFaceDetector =
        FirebaseVision.getInstance().getVisionFaceDetector(options)

    override fun process(bitmap: Bitmap, callback: (Bitmap) -> Unit) {
        val fireImage = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(fireImage)
            .addOnSuccessListener { faces ->
                val bitmapWithFaces = SmileyFaceGraphic().draw(bitmap, faces)
                callback(bitmapWithFaces)
            }
            .addOnFailureListener { e -> Log.e(TAG, "$e") }
    }
}

private const val TAG = "SmileyFaceDetector"