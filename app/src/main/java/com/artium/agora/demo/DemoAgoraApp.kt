package com.artium.agora.demo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.agora.rtc.RtcEngine

@HiltAndroidApp
class DemoAgoraApp : Application() {

    override fun onTerminate() {
        RtcEngine.destroy()
        super.onTerminate()
    }
}