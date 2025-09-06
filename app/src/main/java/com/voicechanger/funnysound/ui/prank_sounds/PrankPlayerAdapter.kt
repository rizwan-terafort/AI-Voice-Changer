package com.voicechanger.funnysound.ui.prank_sounds


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.utils.PrankSoundClickListener

class PrankPlayerAdapter(
    private val context: Context,
    private val list: ArrayList<VoiceEffect>,
    private val listener: PrankSoundClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition = 0
    private var previousPosition = 0

    fun setTheSelectedPosition(position: Int) {
        previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(position)
        notifyItemChanged(previousPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_voices_new, parent, false)
        return FrameViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        (holder as FrameViewHolder)
        holder.category.text = item.name
        Glide.with(holder.img.context).load(item.iconResId).into(holder.img)

        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.bg_rounded_constraintlayout4)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_rounded_constraintlayout3)
        }

        holder.itemView.setOnClickListener {
            listener.onPrankSoundClick(position, list[position])
        }


    }

    inner class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = this.itemView.findViewById(R.id.tv_title)
        val img  : ImageView = itemView.findViewById<ImageView>(R.id.img)
    }

}



