package com.artium.agora.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class MainActivity : AppCompatActivity() {

    // Fill the App ID of your project generated on Agora Console.
    private val appId = "f2b6d3398e0d42418815bee3b7974e5f"

    // Fill the temp token generated on Agora Console.
    private val token =
        "006f2b6d3398e0d42418815bee3b7974e5fIADuStcCHC5IcxOVSP1unCL63oFGjtUdqiXpjOrobLTI0BEfIGgAAAAAEAAhqfJE76U9YgEAAQDspT1i"

    // Fill the channel name.
    private val channelName = "artium_demo_ch"

    private var rtcEngine: RtcEngine? = null

    private val requestPermissionCallbacks = mutableListOf<ActivityResultLauncher<String>>()

    private companion object {
        private const val APP_PERMISSION_REQUEST = 5500
    }

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestCustomPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun intiializeAndJoinChannel() {
        try {
            rtcEngine = RtcEngine.create(this, appId, rtcEngineEventHandler)
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                "Exception ${exception.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        rtcEngine?.enableVideo()
        val videoContainer = findViewById<FrameLayout>(R.id.videoContainer)
        RtcEngine.CreateRendererView(baseContext).let { surfaceView ->
            videoContainer.addView(surfaceView)
            rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
        }

        rtcEngine?.joinChannel(token, channelName, "", 0)
    }

    private fun setUpRemoteUserVideoView(uid: Int) {
        val remoteUserVideoContainer = findViewById<FrameLayout>(R.id.remoteUserContainer)
        val remoteUserSurfaceView = RtcEngine.CreateRendererView(baseContext)
        remoteUserVideoContainer.addView(remoteUserSurfaceView)
        rtcEngine?.setupRemoteVideo(
            VideoCanvas(
                remoteUserSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
    }

    private fun requestCustomPermissions(permissions: Array<String>) {
        val permissionsRequired = mutableListOf<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsRequired.add(permission)
            }
        }

        if (permissionsRequired.isNotEmpty()) {
            requestPermissions(permissionsRequired.toTypedArray(), APP_PERMISSION_REQUEST)
        } else {
            intiializeAndJoinChannel()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == APP_PERMISSION_REQUEST && grantResults.isNotEmpty()) {
            var isGranted = true
            for (grantIndex in grantResults.indices) {
                if (grantResults[grantIndex] == PackageManager.PERMISSION_GRANTED) {
                    isGranted = true
                } else {
                    isGranted = false
                    break
                }
            }

            if (isGranted) {
                intiializeAndJoinChannel()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPermissionCallbacks.forEach { callbacks ->
            callbacks.unregister()
        }
        requestPermissionCallbacks.clear()
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }
}