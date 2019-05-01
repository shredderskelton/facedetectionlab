package com.playground.photofacedetection.video.frame.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.playground.photofacedetection.video.GraphicOverlayView

class FaceOutlineGraphic(
    overlayView: GraphicOverlayView,
    private val firebaseVisionFace: FirebaseVisionFace?
) : GraphicOverlayView.Graphic(overlayView) {
    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas) {
        val face = firebaseVisionFace ?: return
        // Draws a circle at the position of the detected face, with the face's track id below.
        // An offset is used on the Y axis in order to draw the circle, face id and happiness level in the top area
        // of the face's bounding box
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())

        canvas.withSave {
            // Draws a bounding box around the face tilting with the head
            val xOffset = scaleX(face.boundingBox.width() / 2.0f)
            val yOffset = scaleY(face.boundingBox.height() / 2.0f)
            val left = x - xOffset
            val top = y - yOffset
            val right = x + xOffset
            val bottom = y + yOffset
            canvas.rotate(face.headEulerAngleZ, x, y)
            canvas.drawRect(left, top, right, bottom, boxPaint)
        }
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}