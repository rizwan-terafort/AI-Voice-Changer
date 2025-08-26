package com.voicechanger.funnysound.ui.recorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.ItemRecordingBinding

class RecordingAdapter(
    private val recordings: List<Recording>,
    private val onClick: (Recording) -> Unit
) : RecyclerView.Adapter<RecordingAdapter.RecordingViewHolder>() {

    inner class RecordingViewHolder(val binding: ItemRecordingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding = ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val recording = recordings[position]
        holder.binding.tvTitle.text = recording.name
        holder.binding.tvDuration.text = "${formatDuration(recording.duration)} | ${formatFileSize(recording.size)}"
        holder.binding.root.setOnClickListener { onClick(recording) }
    }

    override fun getItemCount(): Int = recordings.size


    fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1) "%.2f MB".format(mb) else "%.2f KB".format(kb)
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
