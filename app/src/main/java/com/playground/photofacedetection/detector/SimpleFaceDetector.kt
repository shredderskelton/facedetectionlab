package com.playground.photofacedetection.detector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.playground.photofacedetection.CameraDirection
import com.playground.photofacedetection.graphic.BlurGraphic

class SimpleFaceDetector(private val context: Context) : FaceDetector {
    private val options = FirebaseVisionFaceDetectorOptions.Builder()
        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
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
                //                val bitmapWithFaces = BoxFacesGraphic.draw(bitmap, faces)
//                val bitmapWithFaces = TrollGraphic(context.resources).draw(bitmap, faces)
                val bitmapWithFaces = BlurGraphic(context, cameraDirection).draw(bitmap, faces)
                callback(bitmapWithFaces)
            }
            .addOnFailureListener { e -> Log.e(TAG, "$e") }
    }
}

private const val TAG = "SimpleFaceDetector"