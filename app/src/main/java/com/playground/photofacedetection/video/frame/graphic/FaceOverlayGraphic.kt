package com.playground.photofacedetection.video.frame.graphic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.playground.photofacedetection.video.GraphicOverlayView

class FaceOverlayGraphic(
    overlayView: GraphicOverlayView,
    private val face: FirebaseVisionFace?,
    private val overlay: Bitmap
) : GraphicOverlayView.Graphic(overlayView) {

    override fun draw(canvas: Canvas) {
        face?.let { face ->
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
                val boundingBox = RectF(left, top, right, bottom)
                canvas.drawBitmap(overlay, null, boundingBox, null)
            }
        }
    }
}