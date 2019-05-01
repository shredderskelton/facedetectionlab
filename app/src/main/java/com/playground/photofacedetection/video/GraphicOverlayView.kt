package com.playground.photofacedetection.video

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.playground.photofacedetection.common.CameraDirection
import com.playground.photofacedetection.video.GraphicOverlayView.Graphic
import java.util.ArrayList

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview). The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.
 *
 *
 * Supports scaling and mirroring of the graphics relative the camera's preview properties. The
 * idea is that detection items are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.
 *
 *
 * Associated [Graphic] items should use the following methods to convert to view
 * coordinates for the graphics that are drawn:
 *
 *
 *  1. [Graphic.scaleX] and [Graphic.scaleY] adjust the size of the
 * supplied value from the preview scale to the view scale.
 *  1. [Graphic.translateX] and [Graphic.translateY] adjust the
 * coordinate from the preview's coordinate system to the view coordinate system.
 *
 */
class GraphicOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val lock = Any()
    private var previewWidth: Int = 0
    private var widthScaleFactor = 1.0f
    private var previewHeight: Int = 0
    private var heightScaleFactor = 1.0f
    private var facing = CameraDirection.BACK
    private val graphics = ArrayList<Graphic>()
    val cameraDirection get() = facing

    /**
     * Base class for a custom graphics object to be rendered within the graphic overlayView. Subclass
     * this and implement the [Graphic.draw] method to define the graphics element. Add
     * instances to the overlayView using [GraphicOverlayView.add].
     */
    abstract class Graphic(private val overlayView: GraphicOverlayView) {
        /**
         * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
         * to view coordinates for the graphics that are drawn:
         *
         *
         *  1. [Graphic.scaleX] and [Graphic.scaleY] adjust the size of the
         * supplied value from the preview scale to the view scale.
         *  1. [Graphic.translateX] and [Graphic.translateY] adjust the
         * coordinate from the preview's coordinate system to the view coordinate system.
         *
         *
         * @param canvas drawing canvas
         */
        abstract fun draw(canvas: Canvas)

        /**
         * Adjusts a horizontal value of the supplied value from the preview scale to the view scale.
         */
        fun scaleX(horizontal: Float): Float {
            return horizontal * overlayView.widthScaleFactor
        }

        /**
         * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
         */
        fun scaleY(vertical: Float): Float {
            return vertical * overlayView.heightScaleFactor
        }

        /**
         * Adjusts the x coordinate from the preview's coordinate system to the view coordinate system.
         */
        fun translateX(x: Float): Float {
            return if (overlayView.facing == CameraDirection.FRONT) {
                overlayView.width - scaleX(x)
            } else {
                scaleX(x)
            }
        }

        /**
         * Adjusts the y coordinate from the preview's coordinate system to the view coordinate system.
         */
        fun translateY(y: Float): Float {
            return scaleY(y)
        }

        fun postInvalidate() {
            overlayView.postInvalidate()
        }
    }

    /**
     * Removes all graphics from the overlay.
     */
    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    /**
     * Adds a graphic to the overlay.
     */
    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
    }

    /**
     * Removes a graphic from the overlay.
     */
    fun remove(graphic: Graphic) {
        synchronized(lock) {
            graphics.remove(graphic)
        }
        postInvalidate()
    }

    /**
     * Sets the camera attributes for size and facing direction, which informs how to transform image
     * coordinates later.
     */
    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: CameraDirection) {
        synchronized(lock) {
            this.previewWidth = previewWidth
            this.previewHeight = previewHeight
            this.facing = facing
        }
        postInvalidate()
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                widthScaleFactor = canvas.width.toFloat() / previewWidth.toFloat()
                heightScaleFactor = canvas.height.toFloat() / previewHeight.toFloat()
            }

            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}
