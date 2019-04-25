package com.playground.photofacedetection.graphic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.Log
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour

class SmileyFaceGraphic : Graphic {
    private val whitePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    override fun draw(bitmap: Bitmap, faces: List<FirebaseVisionFace>): Bitmap {
        val canvas = Canvas(bitmap)
        Log.e("faces", "Face count = ${faces.size}")
        faces.forEach { face ->
            canvas.drawContours(
                listOf(
                    FirebaseVisionFaceContour.FACE,
                    FirebaseVisionFaceContour.RIGHT_EYE,
                    FirebaseVisionFaceContour.LEFT_EYE,
                    FirebaseVisionFaceContour.NOSE_BRIDGE,
                    FirebaseVisionFaceContour.LOWER_LIP_BOTTOM
                ), face
            )
        }
        return bitmap
    }

    private fun Canvas.drawContours(contours: List<Int>, face: FirebaseVisionFace) {
        contours.forEach {
            drawPath(contourToPath(it, face), whitePaint)
        }
    }

    private fun contourToPath(contourId: Int, face: FirebaseVisionFace): Path {
        val points = face.getContour(contourId).points.map { PointF(it.x, it.y) }
        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            points.forEach { path.lineTo(it.x, it.y) }
        } else {
            Log.w("faces", "Points Empty")
        }
        return path
    }
}