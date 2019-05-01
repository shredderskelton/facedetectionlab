package com.playground.photofacedetection.photo.detector

import android.graphics.Bitmap
import com.playground.photofacedetection.common.CameraDirection

interface FaceDetector {
    fun process(
        bitmap: Bitmap,
        callback: (Bitmap) -> Unit,
        cameraDirection: CameraDirection
    )
}