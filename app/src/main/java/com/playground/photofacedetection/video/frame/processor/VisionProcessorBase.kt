package com.playground.photofacedetection.video.frame.processor

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.playground.photofacedetection.video.GraphicOverlayView
import com.playground.photofacedetection.common.BitmapUtils
import com.playground.photofacedetection.common.FrameMetadata
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlayView)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T> : VisionImageProcessor {
    private var previousResults: T? = null
    private var busy = AtomicBoolean(false)
    private var droppedFrameCount = AtomicInteger(0)
    val droppedFrames get() = droppedFrameCount
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlayView: GraphicOverlayView
    ) {
        processImage(data, frameMetadata, graphicOverlayView)
    }

    // Bitmap version
    override fun process(bitmap: Bitmap, graphicOverlayView: GraphicOverlayView) {
        detectInVisionImage(
            null, /* bitmap */
            FirebaseVisionImage.fromBitmap(bitmap),
            null,
            graphicOverlayView
        )
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlayView: GraphicOverlayView
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.width)
            .setHeight(frameMetadata.height)
            .setRotation(frameMetadata.rotation)
            .build()
        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
            bitmap,
            FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata,
            graphicOverlayView
        )
    }

    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlayView: GraphicOverlayView
    ) {
        if (busy.get()) {
            droppedFrameCount.incrementAndGet()
            onSuccess(
                originalCameraImage,
                previousResults,
                metadata!!,
                graphicOverlayView
            )
            return
        }
        busy.set(true)
        droppedFrameCount.set(0)

        detectInImage(image)
            .addOnSuccessListener { results ->
                previousResults = results
                onSuccess(
                    originalCameraImage,
                    results,
                    metadata!!,
                    graphicOverlayView
                )
            }
            .addOnFailureListener { e -> onFailure(e) }
            .addOnCompleteListener {
                // For testing dropped frame functionality
                // Thread(Runnable {
                //    Thread.sleep(2000)
                //    busy.set(false)
                // }).start()
                busy.set(false)
            }
    }

    override fun stop() {}
    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>
    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     * image.
     */
    protected abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: T?,
        frameMetadata: FrameMetadata,
        graphicOverlayView: GraphicOverlayView
    )

    protected abstract fun onFailure(e: Exception)
}

private const val TAG = "VisionsProcessorBase"