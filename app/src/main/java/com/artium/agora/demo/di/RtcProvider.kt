package com.artium.agora.demo.di

import android.content.Context
import com.artium.agora.demo.network.ChannelInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine

@Module
@InstallIn(ViewModelComponent::class)
class RtcProvider {

    @Provides
    fun providesRtcEngine(@ApplicationContext app: Context): RtcHelper = RtcHelper(app)
}

class RtcHelper(private val context: Context) {

    fun initRtcEngine(channelInfo: ChannelInfo): RtcEngine = RtcEngine.create(
        context,
        channelInfo.appId,
        object : IRtcEngineEventHandler() {}
    )
}