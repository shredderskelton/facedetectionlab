package com.playground.photofacedetection.video.frame.graphic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Script
import android.renderscript.ScriptIntrinsicBlur
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.playground.photofacedetection.common.CameraDirection
import com.playground.photofacedetection.video.GraphicOverlayView

private const val BLUR_RADIUS: Float = 25f

class FaceBlurGraphic(
    overlayView: GraphicOverlayView,
    private val faces: List<FirebaseVisionFace>,
    private val context: Context,
    private val cameraBitmap: Bitmap,
    private val cameraDirection: CameraDirection = overlayView.cameraDirection
) : GraphicOverlayView.Graphic(overlayView) {
    override fun draw(canvas: Canvas) {
        val blurredBitmap = faces.foldRight(cameraBitmap) { face, bitmap -> blur(bitmap, face) }
        canvas.drawBitmap(
            blurredBitmap, null,
            Rect(0, 0, canvas.width, canvas.height), null
        )
    }

    private fun blur(image: Bitmap, face: FirebaseVisionFace): Bitmap {
        val outputBitmap = Bitmap.createBitmap(image)
        val renderScript = RenderScript.create(context)
        val tmpIn = Allocation.createFromBitmap(renderScript, image)
        val tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)
        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut, createLaunchOptions(face))
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    private fun createLaunchOptions(face: FirebaseVisionFace): Script.LaunchOptions {
        val limit = Rect(0, 0, cameraBitmap.width, cameraBitmap.height)
        val bounds = with(face.limitedBoundingBox(limit)) {
            if (cameraDirection == CameraDirection.FRONT)
                Rect(cameraBitmap.width - right, top, cameraBitmap.width - left, bottom)
            else
                Rect(left, top, right, bottom)
        }
        return Script.LaunchOptions().apply {
            setX(bounds.left, bounds.right)
            setY(bounds.top, bounds.bottom)
        }
    }

    private fun FirebaseVisionFace.limitedBoundingBox(limit: Rect): Rect {
        return with(boundingBox) {
            Rect(
                left.within(limit.left, limit.right),
                top.within(limit.top, limit.bottom),
                right.within(limit.left, limit.right),
                bottom.within(limit.top, limit.bottom)
            )
        }
    }

    private fun Int.within(min: Int, max: Int): Int = Math.max(Math.min(this, max), min)
}