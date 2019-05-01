package com.playground.photofacedetection.common

import android.hardware.Camera

enum class CameraDirection(val value: Int) {
    FRONT(Camera.CameraInfo.CAMERA_FACING_FRONT),
    BACK(Camera.CameraInfo.CAMERA_FACING_BACK);

    companion object {
        private val lookupTable =
            values().associateBy(CameraDirection::value)

        fun parse(cameraId: Int) = lookupTable[cameraId]
    }
}