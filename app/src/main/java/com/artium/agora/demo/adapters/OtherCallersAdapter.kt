package com.artium.agora.demo.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.artium.agora.demo.databinding.ItemVideoContainerBinding
import com.artium.agora.demo.view.layoutInflater
import com.artium.agora.demo.vm.MultiCallSessionViewModel
import com.artium.agora.demo.vm.UserCallState
import io.agora.rtc.RtcEngine

class OtherCallersAdapter(
    private val baseContext: Context,
    private val callSessionViewModel: MultiCallSessionViewModel
) :
    RecyclerView.Adapter<OtherCallersViewHolder>() {

    private val otherCallers = mutableListOf<UserCallState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherCallersViewHolder {
        return OtherCallersViewHolder(
            ItemVideoContainerBinding.inflate(
                parent.layoutInflater(),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OtherCallersViewHolder, position: Int) {
        holder.bind(baseContext, callSessionViewModel, otherCallers[position])
    }

    override fun onBindViewHolder(
        holder: OtherCallersViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int {
        return otherCallers.size
    }

    fun removeCaller(uid: Int) {
        otherCallers.indexOfFirst { it.uid == uid }.let { index ->
            if (index != -1) {
                otherCallers.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    fun addCaller(callState: UserCallState) {
        otherCallers.add(callState)
        notifyItemInserted(otherCallers.size - 1)
    }

    fun updateControl(callState: UserCallState) {
        otherCallers.indexOfFirst { it.uid == callState.uid }.let { index ->
            if (index != -1) {
                otherCallers[index].isMicOn = callState.isMicOn
                otherCallers[index].isCameraOn = callState.isCameraOn
                notifyItemChanged(index)
            }
        }
    }
}

class OtherCallersViewHolder(private val binding: ItemVideoContainerBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        baseContext: Context,
        callSessionViewModel: MultiCallSessionViewModel,
        callState: UserCallState
    ) {
        RtcEngine.CreateTextureView(baseContext).let { textureView ->
            textureView.scaleX = -1.0f
            binding.itemVideoContainer.addVideoView(textureView, false)
            callSessionViewModel.setUpRemoteVideoView(textureView, callState.uid)
        }

        binding.itemVideoContainer.setMicStatus(callState.isMicOn)
        binding.itemVideoContainer.setVideoStatus(callState.isCameraOn)
    }
}