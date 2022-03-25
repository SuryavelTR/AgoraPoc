package com.artium.agora.demo.vm

import android.view.SurfaceView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import javax.inject.Inject

@HiltViewModel
class CallSessionViewModel @Inject constructor(private val rtcEngine: RtcEngine) : ViewModel() {
    private val token =
        "006f2b6d3398e0d42418815bee3b7974e5fIADuStcCHC5IcxOVSP1unCL63oFGjtUdqiXpjOrobLTI0BEfIGgAAAAAEAAhqfJE76U9YgEAAQDspT1i"

    // Fill the channel name.
    private val channelName = "artium_demo_ch"

    private val uiState = CallSessionUiState(
        isMicOn = true,
        isCameraOn = true,
        isLocalJoined = false,
        isRemoteJoined = false
    )

    private val micStatusChangeLiveData = MutableLiveData(uiState.isMicOn)
    private val cameraStatusChangeLiveData = MutableLiveData(uiState.isCameraOn)
    private val localJoinedLiveData = MutableLiveData(uiState.isLocalJoined)
    private val remoteJoinedLiveData = MutableLiveData<RemoteUserInfo>()

    private val engineEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            localJoinedLiveData.postValue(true)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            remoteJoinedLiveData.postValue(RemoteUserInfo(uid, true))
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            remoteJoinedLiveData.postValue(RemoteUserInfo(uid, false))
        }
    }

    init {
        rtcEngine.addHandler(engineEventHandler)
    }

    fun toggleMic() {
        uiState.isMicOn = !uiState.isMicOn
        micStatusChangeLiveData.value = uiState.isMicOn
        updateMic()
    }

    private fun updateMic() {
        if (uiState.isMicOn) {
            rtcEngine.enableAudio()
        } else {
            rtcEngine.disableAudio()
        }
    }

    fun toggleCamera() {
        uiState.isCameraOn = !uiState.isCameraOn
        cameraStatusChangeLiveData.value = uiState.isCameraOn
        updateCamera()
    }

    private fun updateCamera() {
        if (uiState.isCameraOn) {
            rtcEngine.enableVideo()
        } else {
            rtcEngine.disableVideo()
        }
    }

    fun onMicStatusChange(): LiveData<Boolean> = micStatusChangeLiveData

    fun onCameraStatusChange(): LiveData<Boolean> = cameraStatusChangeLiveData

    fun onLocalJoinedStatusChange(): LiveData<Boolean> = localJoinedLiveData

    fun onRemoteJoinedStatusChange(): LiveData<RemoteUserInfo> = remoteJoinedLiveData

    fun joinCall() {
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)

        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        updateMic()
        updateCamera()

        rtcEngine.joinChannel(token, channelName, "", 0)
    }

    fun setUpLocalVideoView(surfaceView: SurfaceView) {
        rtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    fun setUpRemoteVideoView(surfaceView: SurfaceView, uid: Int) {
        rtcEngine.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    fun leaveCall() {
        rtcEngine.leaveChannel()
    }

    override fun onCleared() {
        super.onCleared()
        RtcEngine.destroy()
    }
}

data class CallSessionUiState(
    var isMicOn: Boolean,
    var isCameraOn: Boolean,
    var isLocalJoined: Boolean,
    var isRemoteJoined: Boolean
)

data class RemoteUserInfo(val uid: Int, val isJoined: Boolean)