package com.playground.photofacedetection.graphic

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.playground.photofacedetection.R

class TrollGraphic(resources: Resources) : Graphic {
    private val trollBitmap = BitmapFactory.decodeResource(resources, R.raw.troll)
    override fun draw(bitmap: Bitmap, faces: List<FirebaseVisionFace>): Bitmap {
        val canvas = Canvas(bitmap)
        faces.forEach { face ->
            canvas.drawBitmap(trollBitmap, null, face.boundingBox, null)
        }
        return bitmap
    }
}