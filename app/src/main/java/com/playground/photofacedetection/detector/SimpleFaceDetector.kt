package com.playground.photofacedetection.detector

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.playground.photofacedetection.graphic.TrollGraphic

class SimpleFaceDetector(private val resources: Resources) : FaceDetector {
    private val options = FirebaseVisionFaceDetectorOptions.Builder()
        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
        .build()
    private val detector: FirebaseVisionFaceDetector =
        FirebaseVision.getInstance().getVisionFaceDetector(options)

    override fun process(bitmap: Bitmap, callback: (Bitmap) -> Unit) {
        val fireImage = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(fireImage)
            .addOnSuccessListener { faces ->
                //                val bitmapWithFaces = BoxFacesGraphic.draw(bitmap, faces)
                val bitmapWithFaces = TrollGraphic(resources).draw(bitmap, faces)
                callback(bitmapWithFaces)
            }
            .addOnFailureListener { e -> Log.e(TAG, "$e") }
    }
}

private const val TAG = "SimpleFaceDetector"