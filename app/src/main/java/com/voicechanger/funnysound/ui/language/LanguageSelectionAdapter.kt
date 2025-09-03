package com.voicechanger.funnysound.ui.language

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.voicechanger.funnysound.R


class LanguageSelectionAdapter(
    private val list: List<LanguageModel>,
    private val listener: LanguageSelectionClickListener
) :
    RecyclerView.Adapter<LanguageSelectionAdapter.LanguageSelectionViewHolder>() {


    inner class LanguageSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var rootLayout: ConstraintLayout? = null
        var name: TextView? = null
        //var icon: ImageView? = null
        var flag: ImageView? = null

        init {
            rootLayout = itemView.findViewById(R.id.rootLayout)
            name = itemView.findViewById(R.id.tv_language)
            //icon = itemView.findViewById(R.id.icon)
            flag = itemView.findViewById(R.id.flag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageSelectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageSelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageSelectionViewHolder, position: Int) {
        val item = list[position]
        holder.name?.text = item.name
        holder.flag?.let { Glide.with(it.context).load(item.icon).into(it) }
        holder.itemView.setOnClickListener {
            list.forEach {
                it.isSelected = false
            }
            item.isSelected = true
            listener.onLanguageClick(item)
            notifyDataSetChanged()
        }


        if (item.isSelected) {
         //   holder.rootLayout?.isActivated = true
            holder.rootLayout?.isActivated = false
            holder.name?.let { name->
                name.context?.let { context: Context ->
                 //   name.setTextColor(ContextCompat.getColor(context,R.color.black))
                    val typeface = ResourcesCompat.getFont(context, R.font.outfit_semibold)
                    name.typeface = typeface
                }

            }

//            holder.icon?.let {
//                Glide.with(it.context).load(R.drawable.ic_radio_checked).into(
//                    it
//                )
//            }
        } else {
            holder.rootLayout?.isActivated = false
            holder.name?.let { name->
                name.context?.let { context: Context ->
                 //   name.setTextColor(ContextCompat.getColor(context,R.color.black))
                    val typeface = ResourcesCompat.getFont(context, R.font.outfit_medium)
                    name.typeface = typeface
                }

            }
//            holder.icon?.let {
//                Glide.with(it.context).load(R.drawable.ic_lan_unselected).into(
//                    it
//                )
//            }
        }
//        if (position % 2 == 0){
//            //left side items
//            holder.rootLayout?.setCustomMargins(40, 40,20,0)
//        }else{
//            //right side items
//            holder.rootLayout?.setCustomMargins(20, 40,40,0)
//        }
    }

    override fun getItemCount(): Int {
        return list.size
    }



    interface LanguageSelectionClickListener {
        fun onLanguageClick(language: LanguageModel?)
    }
}