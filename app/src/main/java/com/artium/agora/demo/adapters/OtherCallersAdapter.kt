package com.artium.agora.demo.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.artium.agora.demo.databinding.ItemVideoContainerBinding
import com.artium.agora.demo.view.layoutInflater

class OtherCallersAdapter : RecyclerView.Adapter<OtherCallersViewHolder>() {

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
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 10
    }
}

class OtherCallersViewHolder(private val binding: ItemVideoContainerBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind() {

    }
}