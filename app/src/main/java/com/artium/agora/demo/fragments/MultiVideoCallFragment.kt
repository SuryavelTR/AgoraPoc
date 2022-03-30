package com.artium.agora.demo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.artium.agora.demo.adapters.OtherCallersAdapter
import com.artium.agora.demo.databinding.FragmentMultiVideoCallBinding
import com.artium.agora.demo.databinding.ItemVideoContainerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MultiVideoCallFragment : Fragment() {

    private lateinit var binding: FragmentMultiVideoCallBinding
    private lateinit var localCallerBinding: ItemVideoContainerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiVideoCallBinding.inflate(inflater, container, false)
        localCallerBinding = ItemVideoContainerBinding.bind(binding.localItemVideoContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.otherCallersRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.otherCallersRecyclerView.adapter = OtherCallersAdapter()
    }
}