package com.artium.agora.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.artium.agora.demo.R
import com.artium.agora.demo.adapters.OtherCallersAdapter
import com.artium.agora.demo.databinding.FragmentMultiVideoCallBinding
import com.artium.agora.demo.databinding.ItemVideoContainerBinding
import com.artium.agora.demo.databinding.ViewMasterControllerBinding
import com.artium.agora.demo.vm.MultiCallSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc.RtcEngine

@AndroidEntryPoint
class MultiVideoCallFragment : Fragment() {

    private lateinit var binding: FragmentMultiVideoCallBinding
    private lateinit var localCallerBinding: ItemVideoContainerBinding
    private lateinit var controllerBinding: ViewMasterControllerBinding

    private val callSessionViewModel by activityViewModels<MultiCallSessionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiVideoCallBinding.inflate(inflater, container, false)
        localCallerBinding = ItemVideoContainerBinding.bind(binding.localItemVideoContainer)
        controllerBinding = ViewMasterControllerBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.otherCallersRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.otherCallersRecyclerView.adapter = OtherCallersAdapter(
            requireActivity().baseContext,
            callSessionViewModel
        )

        setUpControllers()
        setUpLocalSpeaker()
        setUpActiveSpeaker()
        setUpUserObserver()

        callSessionViewModel.joinCall()
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

        controllerBinding.controllerDisconnect.setOnClickListener {
            callSessionViewModel.leaveCall()
            findNavController().popBackStack()
        }
    }

    private fun setUpLocalSpeaker() {
        callSessionViewModel.onLocalUserJoinedTheCall().observe(viewLifecycleOwner) {
            if (it) {
                RtcEngine.CreateTextureView(requireActivity().baseContext).let { textureView ->
                    textureView.scaleX = -1.0f
                    localCallerBinding.itemVideoContainer.addVideoView(textureView, true)
                    callSessionViewModel.setUpLocalVideoView(textureView)
                }
            } else {
                localCallerBinding.itemVideoContainer.removeVideoView()
            }
        }
    }

    private fun setUpActiveSpeaker() {
        callSessionViewModel.onActiveSpeakerChange().observe(viewLifecycleOwner) {
            if (it != null) {
                (binding.otherCallersRecyclerView.adapter as? OtherCallersAdapter)?.removeCaller(it.uid)
                RtcEngine.CreateTextureView(requireActivity().baseContext).let { textureView ->
                    textureView.scaleX = -1.0f
                    binding.focusedCallerVideoContainer.addVideoView(textureView)
                    callSessionViewModel.setUpRemoteVideoView(textureView, it.uid)
                }
                binding.focusedCallerVideoContainer.setVideoStatus(it.isCameraOn)
                binding.focusedCallerVideoContainer.setMicStatus(it.isMicOn)
            } else {
                binding.focusedCallerVideoContainer.removeVideoView()
            }
        }
    }

    private fun setUpUserObserver() {
        callSessionViewModel.onUserControlUpdate().observe(viewLifecycleOwner) {
            if (it.isActiveSpeaker) {
                binding.focusedCallerVideoContainer.setVideoStatus(it.isCameraOn)
                binding.focusedCallerVideoContainer.setMicStatus(it.isMicOn)
            } else {
                (binding.otherCallersRecyclerView.adapter as? OtherCallersAdapter)?.updateControl(it)
            }
        }
        callSessionViewModel.onUserJoined().observe(viewLifecycleOwner) {
            if (!it.isActiveSpeaker) {
                (binding.otherCallersRecyclerView.adapter as? OtherCallersAdapter)?.addCaller(it)
            }
        }
        callSessionViewModel.onUserLeft().observe(viewLifecycleOwner) {
            (binding.otherCallersRecyclerView.adapter as? OtherCallersAdapter)?.removeCaller(it)
        }
    }
}