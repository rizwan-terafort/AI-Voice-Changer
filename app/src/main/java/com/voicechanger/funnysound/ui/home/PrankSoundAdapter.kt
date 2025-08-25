package com.voicechanger.funnysound.ui.home


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.voicechanger.funnysound.R

class PrankSoundAdapter(
    private val context: Context,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItemPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_prank_sound, parent, false)
        return FrameViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      //  val item = list[position]
        (holder as FrameViewHolder)
     //   holder.category.text = item

        holder.itemView.setOnClickListener {
            selectedItemPosition = holder.adapterPosition
            notifyDataSetChanged()
        }


    }

    inner class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = this.itemView.findViewById(R.id.tv_item)
    }






}



