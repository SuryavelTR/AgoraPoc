package com.artium.agora.demo.vm

import android.view.TextureView
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
        "006f2b6d3398e0d42418815bee3b7974e5fIAAyALmfqAePxgyYYELVez7ZbLeYLfGaCr/VZFItw6CX0REfIGgAAAAAEAAJmhJvC+I+YgEAAQAL4j5i"

    // Fill the channel name.
    private val channelName = "artium_demo_ch"

    private val uiState = CallSessionUiState()

    private val micStatusChangeLiveData = MutableLiveData(uiState.isMicOn)
    private val cameraStatusChangeLiveData = MutableLiveData(uiState.isCameraOn)
    private val localJoinedLiveData = MutableLiveData(uiState.isLocalJoined)
    private val remoteJoinedLiveData = MutableLiveData<RemoteUserInfo>()

    //remote
    private val remoteMicStatusChangeLiveData = MutableLiveData(uiState.isRemoteMicOn)
    private val remoteVideoStatusChangeLiveData = MutableLiveData(uiState.isRemoteVideoOn)

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

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
        }

        //region Video
        override fun onFacePositionChanged(
            imageWidth: Int,
            imageHeight: Int,
            faces: Array<out AgoraFacePositionInfo>?
        ) {
            super.onFacePositionChanged(imageWidth, imageHeight, faces)
        }

        override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
            super.onUserEnableVideo(uid, enabled)
        }

        override fun onUserEnableLocalVideo(uid: Int, enabled: Boolean) {
            super.onUserEnableLocalVideo(uid, enabled)
            remoteVideoStatusChangeLiveData.postValue(enabled)
        }

        /**
         * Provides the video state for local user.
         * 0 - disabled
         * 1 -
         * 2 - Enabled
         */
//        override fun onLocalVideoStateChanged(localVideoState: Int, error: Int) {
//            super.onLocalVideoStateChanged(localVideoState, error)
//        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            remoteVideoStatusChangeLiveData.postValue(state == Constants.REMOTE_VIDEO_STATE_DECODING)
        }

//        override fun onLocalVideoStats(stats: LocalVideoStats?) {
//            super.onLocalVideoStats(stats)
//        }

//        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
//            super.onRemoteVideoStats(stats)
//        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            super.onUserMuteVideo(uid, muted)
            remoteVideoStatusChangeLiveData.postValue(!muted)
        }

        //endregion

        //region Audio
        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            super.onUserMuteAudio(uid, muted)
            remoteMicStatusChangeLiveData.postValue(!muted)
        }

        /**
         * Provides the audio state for local user.
         * 0 - disabled
         * 1 -
         * 2 - Enabled
         */
//        override fun onLocalAudioStateChanged(state: Int, error: Int) {
//            super.onLocalAudioStateChanged(state, error)
//        }

        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
            remoteMicStatusChangeLiveData.postValue(state == Constants.REMOTE_AUDIO_STATE_DECODING)
        }

//        override fun onLocalAudioStats(stats: LocalAudioStats?) {
//            super.onLocalAudioStats(stats)
//        }

//        override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
//            super.onRemoteAudioStats(stats)
//        }

        override fun onAudioVolumeIndication(
            speakers: Array<out AudioVolumeInfo>?,
            totalVolume: Int
        ) {
            super.onAudioVolumeIndication(speakers, totalVolume)
        }
        //endregion
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
        rtcEngine.muteLocalAudioStream(!uiState.isMicOn)
//        if (uiState.isMicOn) rtcEngine.enableLocalAudio(true) else rtcEngine.enableLocalAudio(false)

    }

    fun toggleCamera() {
        uiState.isCameraOn = !uiState.isCameraOn
        cameraStatusChangeLiveData.value = uiState.isCameraOn
        updateCamera()
    }

    private fun updateCamera() {
        rtcEngine.muteLocalVideoStream(!uiState.isCameraOn)
//        if (uiState.isCameraOn) rtcEngine.enableVideo() else rtcEngine.enableVideo()
    }

    fun onMicStatusChange(): LiveData<Boolean> = micStatusChangeLiveData

    fun onCameraStatusChange(): LiveData<Boolean> = cameraStatusChangeLiveData

    fun onLocalJoinedStatusChange(): LiveData<Boolean> = localJoinedLiveData

    fun onRemoteJoinedStatusChange(): LiveData<RemoteUserInfo> = remoteJoinedLiveData

    fun onRemoteMicStatusChange(): LiveData<Boolean> = remoteMicStatusChangeLiveData

    fun onRemoteVideoStatusChange(): LiveData<Boolean> = remoteVideoStatusChangeLiveData

    fun joinCall() {
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)

        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        rtcEngine.enableVideo()

        updateMic()
        updateCamera()

        rtcEngine.joinChannel(token, channelName, "", 0)
    }

    fun setUpLocalVideoView(textureView: TextureView) {
        rtcEngine.setupLocalVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_FILL, 0))
    }

    fun setUpRemoteVideoView(textureView: TextureView, uid: Int) {
        rtcEngine.setupRemoteVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_FIT, uid))
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
    var isMicOn: Boolean = true,
    var isCameraOn: Boolean = true,
    var isLocalJoined: Boolean = false,
    var isRemoteJoined: Boolean = false,
    var isRemoteMicOn: Boolean = false,
    var isRemoteVideoOn: Boolean = false
)

data class RemoteUserInfo(val uid: Int, val isJoined: Boolean)