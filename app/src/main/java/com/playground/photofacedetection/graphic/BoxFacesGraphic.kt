package com.playground.photofacedetection.graphic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.face.FirebaseVisionFace

object BoxFacesGraphic : Graphic {
    private val whitePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    override fun draw(bitmap: Bitmap, faces: List<FirebaseVisionFace>): Bitmap {
        val canvas = Canvas(bitmap)
        faces.forEach { face ->
            canvas.drawRect(face.boundingBox, whitePaint)
        }
        return bitmap
    }
}