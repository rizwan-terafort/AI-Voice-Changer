package com.voicechanger.funnysound.ui.recorder.voice_effect.bg_music

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.databinding.ItemVoicesBinding
import com.voicechanger.funnysound.databinding.ItemVoiceEffectExpandedBinding
import com.voicechanger.funnysound.ui.recorder.voice_effect.SpeedAdjustListener
import com.voicechanger.funnysound.utils.toIntValue

////
class BgMusicAdapter(
    private val voiceEffects: List<VoiceEffect>,
    private val spanCount: Int,
    private val onParentClick: (VoiceEffect) -> Unit,
    private val listener: BgMusicVolumeChangeListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_NORMAL = 1
        const val VIEW_TYPE_EXPANDED = 2
    }

    private var expandedPosition = -1
    private val displayItems = mutableListOf<DisplayItem>()

    init { rebuildDisplayItems() }

    sealed class DisplayItem {
        data class Normal(val originalIndex: Int, val voiceEffect: VoiceEffect) : DisplayItem()
        data class Expanded(val originalIndex: Int, val voiceEffect: VoiceEffect) : DisplayItem()
    }

    override fun getItemViewType(position: Int): Int = when (displayItems[position]) {
        is DisplayItem.Normal   -> VIEW_TYPE_NORMAL
        is DisplayItem.Expanded -> VIEW_TYPE_EXPANDED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_NORMAL -> NormalViewHolder(
                ItemVoicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_EXPANDED -> ExpandedViewHolder(
                ItemVoiceEffectExpandedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> error("Invalid view type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.Normal -> {
                val v = item.voiceEffect
                (holder as NormalViewHolder).binding.apply {
                    tvTitle.text = v.name
                    img.setImageResource(v.iconResId)
                    root.setOnClickListener {
                        if (position !=0) expandItem(item.originalIndex)
                        onParentClick(v)
                        listener.onVolumeChanged(-1, item.voiceEffect)
                    }
                }
            }
            is DisplayItem.Expanded -> {
                val v = item.voiceEffect
                val originalIdx = item.originalIndex

                (holder as ExpandedViewHolder).binding.apply {

                    tvSpeed.visibility = View.GONE
                    tv00.visibility = View.GONE
                    seekBarSpeed.visibility = View.GONE
                    tv300.visibility = View.GONE

                    tvVoiceEffect.text = "Volume"
                    tv30.text = "100"
                    seekBarVoiceEffect.max = 100
                    seekBarVoiceEffect.progress = 50

                    root.setOnClickListener {
                       // collapseItem()
                    }

                    seekBarVoiceEffect.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            listener.onVolumeChanged(progress, item.voiceEffect)
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })

                }
            }
        }
    }


    override fun getItemCount(): Int = displayItems.size

    private fun expandItem(originalIndex: Int) {
        if (expandedPosition == originalIndex) {
            collapseItem()
            return
        }
        expandedPosition = originalIndex
        rebuildDisplayItems()
        notifyDataSetChanged()
    }

    private fun collapseItem() {
        if (expandedPosition != -1) {
            expandedPosition = -1
            rebuildDisplayItems()
            notifyDataSetChanged()
        }
    }


    private fun rebuildDisplayItems() {
        displayItems.clear()
        // 1) push all normals
        voiceEffects.forEachIndexed { idx, ve ->
            displayItems.add(DisplayItem.Normal(idx, ve))
        }

        // 2) inject expanded full-width row after the anchor row end
        if (expandedPosition != -1) {
            val rowStart = (expandedPosition / spanCount) * spanCount
            val rowEnd = minOf(rowStart + spanCount - 1, voiceEffects.lastIndex)

            val insertAfterIndex = displayItems.indexOfLast {
                it is DisplayItem.Normal && it.originalIndex == rowEnd
            }
            // keep original index of the clicked item
            val expandedItem = DisplayItem.Expanded(originalIndex = expandedPosition, voiceEffect = voiceEffects[expandedPosition])
            displayItems.add(insertAfterIndex + 1, expandedItem)
        }
    }

    inner class NormalViewHolder(val binding: ItemVoicesBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ExpandedViewHolder(val binding: ItemVoiceEffectExpandedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ExpandedViewHolder) {
            holder.binding.seekBarVoiceEffect.setOnSeekBarChangeListener(null)
        }
        super.onViewRecycled(holder)
    }

}

interface BgMusicVolumeChangeListener{
    fun onVolumeChanged(value : Int, item : VoiceEffect)
}