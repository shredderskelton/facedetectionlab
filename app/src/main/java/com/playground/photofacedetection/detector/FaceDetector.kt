package com.playground.photofacedetection.detector

import android.graphics.Bitmap

interface FaceDetector {
    fun process(bitmap: Bitmap, callback: (Bitmap) -> Unit)
}