package com.voicechanger.funnysound.ui.recorder.voice_effect

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.databinding.ItemVoicesBinding
import com.voicechanger.funnysound.databinding.ItemVoiceEffectExpandedBinding
////
class VoicesAdapter(
    private val voiceEffects: List<VoiceEffect>,
    private val spanCount: Int,
    private val onParentClick: (VoiceEffect) -> Unit,
    private val listener: SpeedAdjustListener
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
                        expandItem(item.originalIndex)
                        onParentClick(v)
                    }
                }
            }
            is DisplayItem.Expanded -> {
                val v = item.voiceEffect
                val originalIdx = item.originalIndex

                (holder as ExpandedViewHolder).binding.apply {
                    root.setOnClickListener { collapseItem() }

                    seekBarVoiceEffect.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                            listener.onAdjust(originalIdx, false, progress)
                        }
                        override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
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

    /**
     * Core: insert the Expanded row AFTER the row that contains `expandedPosition`.
     */
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

interface SpeedAdjustListener {
    fun onAdjust(position: Int, isSpeed : Boolean, progress : Int)
}