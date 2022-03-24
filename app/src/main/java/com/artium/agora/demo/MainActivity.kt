package com.artium.agora.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
        "0067eae3982dde04b9a8e986109c307682cIAC1N07bM5Z21Mk5A512H9xQgwd6I8O0U8qZ+lEZ4bM/6REfIGgAAAAAEABHuwtvEzs8YgEAAQATOzxi"

    // Fill the channel name.
    private val channelName = "artium_demo_ch"

    private var rtcEngine: RtcEngine? = null

    private val requestPermissionCallbacks = mutableListOf<ActivityResultLauncher<String>>()

    private companion object {
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
        private const val APP_PERMISSION_REQUEST = 5500
    }

    private val rtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        getPermissionResultCallback {
//            getPermissionResultCallback().let {
//                requestPermissionCallbacks.add(it)
//                requestPermission(it, Manifest.permission.RECORD_AUDIO)
//            }
//        }.let {
//            requestPermissionCallbacks.add(it)
//            requestPermission(it, Manifest.permission.CAMERA)
//        }

        requestCustomPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun getPermissionResultCallback(onGranted: () -> Unit = {}) =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
//                    intiializeAndJoinChannel()
                onGranted()
                Toast
                    .makeText(this, "Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast
                    .makeText(this, "Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
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

    private fun requestPermission(
        requestPermissionCallback: ActivityResultLauncher<String>,
        permission: String
    ) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // TODO: ignore
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // TODO: Custom justification for the permission
            }
            else -> {
                requestPermissionCallback.launch(permission)
            }
        }
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

        requestPermissions(permissionsRequired.toTypedArray(), APP_PERMISSION_REQUEST)
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