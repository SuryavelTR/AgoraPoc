package com.artium.agora.demo.di

import android.content.Context
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
    fun providesRtcEngine(@ApplicationContext app: Context): RtcEngine =
        RtcEngine.create(
            app,
            "f2b6d3398e0d42418815bee3b7974e5f",
            object : IRtcEngineEventHandler() {}
        )
}