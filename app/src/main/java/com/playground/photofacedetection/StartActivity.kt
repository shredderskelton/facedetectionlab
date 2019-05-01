package com.playground.photofacedetection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.playground.photofacedetection.common.Effect
import com.playground.photofacedetection.photo.MainActivity
import com.playground.photofacedetection.video.RealTimeActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        videoBoxButton.setOnClickListener {
            RealTimeActivity.start(this, Effect.BOX)
        }

        videoContourButton.setOnClickListener {
            RealTimeActivity.start(this, Effect.OUTLINE)
        }

        videoTrollButton.setOnClickListener {
            RealTimeActivity.start(this, Effect.TROLL)
        }

        videoBlurButton.setOnClickListener {
            RealTimeActivity.start(this, Effect.BLUR)
        }

        photoBoxButton.setOnClickListener {
            MainActivity.start(this, Effect.BOX)
        }

        photoContourButton.setOnClickListener {
            MainActivity.start(this, Effect.OUTLINE)
        }

        photoTrollButton.setOnClickListener {
            MainActivity.start(this, Effect.TROLL)
        }

        photoBlurButton.setOnClickListener {
            MainActivity.start(this, Effect.BLUR)
        }
    }
}