package com.artium.agora.demo.vm

import android.view.TextureView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.artium.agora.demo.di.RtcHelper
import com.artium.agora.demo.network.ApiClient
import com.artium.agora.demo.network.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class MultiCallSessionViewModel @Inject constructor(
    private val rtcHelper: RtcHelper,
    private val apiClient: ApiClient
) : ViewModel() {

    private var channelInfo: ChannelInfo? = null
    private lateinit var rtcEngine: RtcEngine

    private val localUserState = UserCallState(0)
    private val otherUserCallState = mutableListOf<UserCallState>()

    private val localSpeakerLiveData = MutableLiveData(false)
    private val activeSpeakerLiveData = MutableLiveData<UserCallState?>()
    private val micStatusChangeLiveData = MutableLiveData(localUserState.isMicOn)
    private val cameraStatusChangeLiveData = MutableLiveData(localUserState.isCameraOn)
    private val userAddedLiveData = MutableLiveData<UserCallState>()
    private val removeUserLiveData = MutableLiveData<Int>()
    private val userUpdateControlLiveData = MutableLiveData<UserCallState>()

    fun initiateRtcEngine(info: ChannelInfo) {
        channelInfo = info
        rtcEngine = rtcHelper.initRtcEngine(info)
        rtcEngine.addHandler(engineEventHandler)
    }

    private val engineEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            localUserState.uid = uid
            localSpeakerLiveData.postValue(true)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            val userCallState = UserCallState(uid)
            otherUserCallState.add(userCallState)

            if (activeSpeakerLiveData.value == null && otherUserCallState.isNotEmpty()) {
                makeActiveSpeaker(otherUserCallState.first().uid)
            }
            userAddedLiveData.postValue(userCallState)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)

            if (uid == activeSpeakerLiveData.value?.uid) {
                activeSpeakerLiveData.postValue(null)
            }

            otherUserCallState.removeIf { state ->
                return@removeIf state.uid == uid
            }

            if (activeSpeakerLiveData.value == null && otherUserCallState.isNotEmpty()) {
                makeActiveSpeaker(otherUserCallState.first().uid)
            }

            removeUserLiveData.postValue(uid)
        }

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
            makeActiveSpeaker(uid)
        }

        //region Video
        override fun onFacePositionChanged(
            imageWidth: Int,
            imageHeight: Int,
            faces: Array<out AgoraFacePositionInfo>?
        ) {
            super.onFacePositionChanged(imageWidth, imageHeight, faces)
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            super.onUserMuteVideo(uid, muted)
            otherUserCallState.find { it.uid == uid }?.let {
                it.isCameraOn = !muted
                userUpdateControlLiveData.postValue(it)
            }
        }

        //endregion

        //region Audio
        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            super.onUserMuteAudio(uid, muted)
            otherUserCallState.find { it.uid == uid }?.let {
                it.isMicOn = !muted
                userUpdateControlLiveData.postValue(it)
            }
        }
        //endregion
    }

    private fun makeActiveSpeaker(uid: Int) {
        otherUserCallState.find { it.isActiveSpeaker }?.let {
            if (it.uid != uid) {
                it.isActiveSpeaker = false
                userAddedLiveData.postValue(it)
            }
        }
        otherUserCallState.find { it.uid == uid }?.let {
            it.isActiveSpeaker = true
            activeSpeakerLiveData.postValue(it)
        }
    }

    fun onLocalUserJoinedTheCall(): LiveData<Boolean> = localSpeakerLiveData

    fun onActiveSpeakerChange(): LiveData<UserCallState?> = activeSpeakerLiveData

    fun onUserJoined(): LiveData<UserCallState> = userAddedLiveData

    fun onUserLeft(): LiveData<Int> = removeUserLiveData

    fun onUserControlUpdate():LiveData<UserCallState> = userUpdateControlLiveData

    fun toggleMic() {
        localUserState.isMicOn = !localUserState.isMicOn
        micStatusChangeLiveData.value = localUserState.isMicOn
        updateMic()
    }

    private fun updateMic() {
        rtcEngine.muteLocalAudioStream(!localUserState.isMicOn)
    }

    fun toggleCamera() {
        localUserState.isCameraOn = !localUserState.isCameraOn
        cameraStatusChangeLiveData.value = localUserState.isCameraOn
        updateCamera()
    }

    private fun updateCamera() {
        rtcEngine.muteLocalVideoStream(!localUserState.isCameraOn)
    }

    fun onMicStatusChange(): LiveData<Boolean> = micStatusChangeLiveData

    fun onCameraStatusChange(): LiveData<Boolean> = cameraStatusChangeLiveData

    fun joinCall() {
        channelInfo?.let { info ->
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)

            rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

            rtcEngine.enableVideo()

            updateMic()
            updateCamera()

            rtcEngine.joinChannel(info.token, info.channelName, "", 0)
        }
    }

    fun setUpLocalVideoView(textureView: TextureView) {
        rtcEngine.setupLocalVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_FILL, 0))
    }

    fun setUpRemoteVideoView(textureView: TextureView, uid: Int) {
        rtcEngine.setupRemoteVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    fun leaveCall() {
        rtcEngine.leaveChannel()
        otherUserCallState.clear()
    }

    fun retrieveChannelInfo() = liveData(Dispatchers.IO) {
        try {
            emit(apiClient.fetchCallInfo())
        } catch (exception: Exception) {
            emit(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        RtcEngine.destroy()
    }
}

data class UserCallState(
    var uid: Int,
    var isMicOn: Boolean = true,
    var isCameraOn: Boolean = true,
    var isActiveSpeaker: Boolean = false,
)