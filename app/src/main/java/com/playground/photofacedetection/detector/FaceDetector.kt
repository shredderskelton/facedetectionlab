package com.playground.photofacedetection.detector

import android.graphics.Bitmap
import com.playground.photofacedetection.CameraDirection

interface FaceDetector {
    fun process(
        bitmap: Bitmap,
        callback: (Bitmap) -> Unit,
        cameraDirection: CameraDirection
    )
}