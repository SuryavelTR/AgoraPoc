package com.artium.agora.demo.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.artium.agora.demo.R
import com.artium.agora.demo.databinding.ViewVideoContainerBinding

class VideoContainerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: ViewVideoContainerBinding =
        ViewVideoContainerBinding.inflate(layoutInflater(), this)

    fun addVideoView(view: View, isLocal: Boolean = false) {
        binding.videoContainer.addView(view)
        binding.indicatorContainer.isVisible = !isLocal
    }

    fun removeVideoView() {
        binding.videoContainer.removeAllViews()
    }

    fun setMicStatus(isOn: Boolean) {
        binding.micIndicator.setImageResource(
            if (isOn) R.drawable.ic_mic_on else R.drawable.ic_mic_off
        )
    }

    fun setVideoStatus(isOn: Boolean) {
        binding.cameraIndicator.setImageResource(
            if (isOn) R.drawable.ic_videocam_on else R.drawable.ic_videocam_off
        )
    }
}


fun View.layoutInflater() = LayoutInflater.from(context)