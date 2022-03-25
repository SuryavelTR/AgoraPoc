package com.artium.agora.demo.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.artium.agora.demo.R
import com.artium.agora.demo.databinding.ActivityInteractiveLiveStreamingBinding
import com.artium.agora.demo.databinding.ViewMasterControllerBinding
import com.artium.agora.demo.vm.CallSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import javax.inject.Inject

//@AndroidEntryPoint
class InteractiveLiveStreamingActivity : BaseActivity() {

    // Fill the App ID of your project generated on Agora Console.
    private val appId = "f2b6d3398e0d42418815bee3b7974e5f"

    // Fill the temp token generated on Agora Console.
    private val token =
        "006f2b6d3398e0d42418815bee3b7974e5fIADuStcCHC5IcxOVSP1unCL63oFGjtUdqiXpjOrobLTI0BEfIGgAAAAAEAAhqfJE76U9YgEAAQDspT1i"

    // Fill the channel name.
    private val channelName = "artium_demo_ch"

    @Inject
    lateinit var rtcEngine: RtcEngine

    private lateinit var binding: ActivityInteractiveLiveStreamingBinding
    private lateinit var controllerBinding: ViewMasterControllerBinding

    val callSessionViewModel by viewModels<CallSessionViewModel>()


    private val rtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                setUpRemoteUserVideoView(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            super.onUserMuteVideo(uid, muted)
        }

        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            super.onUserMuteAudio(uid, muted)
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInteractiveLiveStreamingBinding.inflate(layoutInflater)
        controllerBinding = ViewMasterControllerBinding.bind(binding.root)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        callSessionViewModel.onMicStatusChange().observe(this) { enabled ->
            controllerBinding.controllerMic.setImageResource(
                if (enabled) R.drawable.ic_mic_on else R.drawable.ic_mic_off
            )

            if (enabled) {
                rtcEngine.enableAudio()
            } else {
                rtcEngine.disableAudio()
            }
        }

        callSessionViewModel.onCameraStatusChange().observe(this) { enabled ->
            controllerBinding.controllerCamera.setImageResource(
                if (enabled) R.drawable.ic_videocam_on else R.drawable.ic_videocam_off
            )

            if (enabled) {
                rtcEngine.enableVideo()
            } else {
                rtcEngine.disableVideo()
            }
        }

        controllerBinding.controllerMic.setOnClickListener {
            callSessionViewModel.toggleMic()
        }

        controllerBinding.controllerCamera.setOnClickListener {
            callSessionViewModel.toggleCamera()
        }
    }

    override fun onReceivedAllPermissions() {
        initializeAndJoinCall()
    }

    private fun initializeAndJoinCall() {
//        try {
//            rtcEngine = RtcEngine.create(this, appId, rtcEngineEventHandler)
//        } catch (exception: Exception) {
//            Toast.makeText(
//                this,
//                "Exception ${exception.localizedMessage}",
//                Toast.LENGTH_SHORT
//            ).show()
//            return
//        }

        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)

        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        RtcEngine.CreateRendererView(baseContext).let { surfaceView ->
            binding.localVideoContainer.addVideoView(surfaceView, true)
            rtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
        }

        rtcEngine.joinChannel(token, channelName, "", 0)
    }

    private fun setUpRemoteUserVideoView(uid: Int) {
        val remoteUserSurfaceView = RtcEngine.CreateRendererView(baseContext)
        binding.remoteVideoContainer.let {
            it.isVisible = true
            it.addVideoView(remoteUserSurfaceView)
        }
        rtcEngine.setupRemoteVideo(
            VideoCanvas(
                remoteUserSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine.leaveChannel()
        RtcEngine.destroy()
    }
}