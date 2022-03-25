package com.artium.agora.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.artium.agora.demo.R
import com.artium.agora.demo.databinding.FragmentVideoCallBinding
import com.artium.agora.demo.databinding.ViewMasterControllerBinding
import com.artium.agora.demo.vm.CallSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc.RtcEngine

@AndroidEntryPoint
class VideoCallFragment : Fragment() {

    private lateinit var binding: FragmentVideoCallBinding
    private lateinit var controllerBinding: ViewMasterControllerBinding

    private val callSessionViewModel by activityViewModels<CallSessionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoCallBinding.inflate(layoutInflater, container, false)
        controllerBinding = ViewMasterControllerBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpControllers()
        setUpVideoCall()
    }

    private fun setUpControllers() {
        callSessionViewModel.onMicStatusChange().observe(viewLifecycleOwner) { enabled ->
            controllerBinding.controllerMic.setImageResource(
                if (enabled) R.drawable.ic_mic_on else R.drawable.ic_mic_off
            )
        }

        callSessionViewModel.onCameraStatusChange().observe(this) { enabled ->
            controllerBinding.controllerCamera.setImageResource(
                if (enabled) R.drawable.ic_videocam_on else R.drawable.ic_videocam_off
            )
        }

        controllerBinding.controllerMic.setOnClickListener {
            callSessionViewModel.toggleMic()
        }

        controllerBinding.controllerCamera.setOnClickListener {
            callSessionViewModel.toggleCamera()
        }
    }

    private fun setUpVideoCall() {
        callSessionViewModel.onLocalJoinedStatusChange().observe(viewLifecycleOwner) { joined ->
            if (joined) {
                RtcEngine.CreateRendererView(requireActivity().baseContext).let { surfaceView ->
                    binding.localVideoContainer.addVideoView(surfaceView, true)
                    callSessionViewModel.setUpLocalVideoView(surfaceView)
                }
            } else {
                binding.localVideoContainer.removeVideoView()
            }
        }

        callSessionViewModel.onRemoteJoinedStatusChange().observe(viewLifecycleOwner) { userInfo ->
            binding.remoteVideoContainer.isVisible = userInfo.isJoined
            if (userInfo.isJoined) {
                RtcEngine.CreateRendererView(requireActivity().baseContext).let { surfaceView ->
                    binding.remoteVideoContainer.addVideoView(surfaceView)
                    callSessionViewModel.setUpRemoteVideoView(surfaceView, userInfo.uid)
                }
            } else {
                binding.remoteVideoContainer.removeVideoView()
            }
        }

        callSessionViewModel.joinCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        callSessionViewModel.leaveCall()
    }
}