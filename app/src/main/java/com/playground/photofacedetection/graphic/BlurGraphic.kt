package com.playground.photofacedetection.graphic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Script
import android.renderscript.ScriptIntrinsicBlur
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.playground.photofacedetection.CameraDirection

private const val BLUR_RADIUS = 25f

class BlurGraphic(
    private val context: Context,
    private val cameraDirection: CameraDirection
) : Graphic {
    override fun draw(bitmap: Bitmap, faces: List<FirebaseVisionFace>): Bitmap {
        return faces.foldRight(bitmap) { face, accBitmap -> blur(accBitmap, face) }
    }

    private fun blur(image: Bitmap, face: FirebaseVisionFace): Bitmap {
        val outputBitmap = Bitmap.createBitmap(image)
        val renderScript = RenderScript.create(context)
        val tmpIn = Allocation.createFromBitmap(renderScript, image)
        val tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)
        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut, createLaunchOptions(image, face))
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    private fun createLaunchOptions(
        cameraBitmap: Bitmap,
        face: FirebaseVisionFace
    ): Script.LaunchOptions {
        val limit = Rect(0, 0, cameraBitmap.width, cameraBitmap.height)
        val bounds = with(face.limitedBoundingBox(limit)) {
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