package com.artium.agora.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.artium.agora.demo.databinding.FragmentPreJoinBinding

class PreJoinCallFragment : Fragment() {

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
            findNavController().navigate(PreJoinCallFragmentDirections.preJoinToVideoCallFragment())
        }
    }
}