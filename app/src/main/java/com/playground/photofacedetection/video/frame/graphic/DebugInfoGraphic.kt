package com.playground.photofacedetection.video.frame.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.playground.photofacedetection.video.GraphicOverlayView

class DebugInfoGraphic(
    overlayView: GraphicOverlayView,
    private val droppedFrames: Int
) : GraphicOverlayView.Graphic(overlayView) {

    private val idPaint = Paint().apply {
        color = Color.WHITE
        textSize = 30.0f
    }

    override fun draw(canvas: Canvas) {
        canvas.drawText(
            "Dropped frames: $droppedFrames",
            50f,
            50f,
            idPaint
        )
    }
}