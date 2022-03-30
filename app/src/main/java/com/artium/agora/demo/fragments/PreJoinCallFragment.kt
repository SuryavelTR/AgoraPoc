package com.artium.agora.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.artium.agora.demo.databinding.FragmentPreJoinBinding
import com.artium.agora.demo.network.ChannelInfo
import com.artium.agora.demo.vm.CallSessionViewModel
import com.artium.agora.demo.vm.MultiCallSessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreJoinCallFragment : Fragment() {

    private val callSessionViewModel by activityViewModels<CallSessionViewModel>()
    private val multiCallSessionViewModel by activityViewModels<MultiCallSessionViewModel>()

    private lateinit var binding: FragmentPreJoinBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreJoinBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.joinButton.setOnClickListener {
            fetchChannelInfo {
                callSessionViewModel.initiateRtcEngine(it)
                findNavController()
                    .navigate(PreJoinCallFragmentDirections.preJoinToVideoCallFragment())
            }
        }

        binding.joinGroupButton.setOnClickListener {
            fetchChannelInfo {
                multiCallSessionViewModel.initiateRtcEngine(it)
                findNavController()
                    .navigate(PreJoinCallFragmentDirections.preJoinToMultiVideoCallFragment())
            }
        }
    }

    private fun fetchChannelInfo(onSuccess: (channelInfo: ChannelInfo) -> Unit) {
        binding.buttonContainer.isVisible = false
        binding.progressIndicator.isVisible = true
        callSessionViewModel.retrieveChannelInfo().observe(viewLifecycleOwner) {
            binding.buttonContainer.isVisible = true
            binding.progressIndicator.isVisible = false
            if (it != null) {
                onSuccess(it)
            }
        }
    }
}