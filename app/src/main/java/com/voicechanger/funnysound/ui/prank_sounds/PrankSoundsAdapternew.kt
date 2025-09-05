package com.voicechanger.funnysound.ui.prank_sounds


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.utils.PrankSoundClickListener

class PrankSoundsAdapternew(
    private val context: Context,
    private val list : ArrayList<VoiceEffect>,
    private val listener : PrankSoundClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_voices, parent, false)
        return FrameViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
          val item = list[position]

        (holder as FrameViewHolder)
        holder.category.text = item.name

        holder.itemView.setOnClickListener {
           listener.onPrankSoundClick(position, list[position])
        }


    }

    inner class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = this.itemView.findViewById(R.id.tv_title)
    }

}



