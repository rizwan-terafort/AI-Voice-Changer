package com.voicechanger.funnysound.ui.recorder.voice_effect

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.ItemRecordingBinding
import com.voicechanger.funnysound.databinding.ItemVoicesBinding

class VoicesAdapter(
    private val recordings : ArrayList<Recording>,
    private val onClick: (Recording) -> Unit
) : RecyclerView.Adapter<VoicesAdapter.RecordingViewHolder>() {

    inner class RecordingViewHolder(val binding: ItemVoicesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding = ItemVoicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
       // val recording = recordings[position]
        holder.binding.tvTitle.text = "Title"
     //   holder.binding.root.setOnClickListener { onClick() }
        holder.binding.root.setOnClickListener {
            Toast.makeText(holder.binding.tvTitle.context, "clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = 10


}