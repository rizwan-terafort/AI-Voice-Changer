package com.voicechanger.funnysound.ui.recorder.voice_effect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.databinding.FragmentVoicesBinding

class VoicesFragment : Fragment(), SpeedAdjustListener {
    private var binding : FragmentVoicesBinding? = null

    private val voiceEffects = mutableListOf<VoiceEffect>()



    private lateinit var voicesAdapter: VoicesAdapter

    private val spanCount = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoicesBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVoiceEffects()
        setupRecyclerView()
    }

    private fun setupVoiceEffects() {
        voiceEffects.addAll(listOf(
            VoiceEffect(0,"Default", R.drawable.ic_mic_new,0f,0f),
            VoiceEffect(1,"Girl", R.drawable.ic_mic_new, 1.4f, 1.0f),
            VoiceEffect(2,"Boy", R.drawable.ic_mic_new, 1.2f, 1.0f),
            VoiceEffect(3,"Child", R.drawable.ic_mic_new,1.6f, 1.0f),
            VoiceEffect(4,"Old", R.drawable.ic_mic_new,0.8f, 0.9f),
            VoiceEffect(5,"Basso", R.drawable.ic_mic_new,0.6f, 1.0f),
            VoiceEffect(6,"Small Robot", R.drawable.ic_mic_new,1.5f, 1.2f),
            VoiceEffect(7,"Soprano", R.drawable.ic_mic_new,1.7f, 1.0f),
            VoiceEffect(8,"Cave", R.drawable.ic_mic_new,0.7f, 0.9f)
        ))
    }

    private fun setupRecyclerView() {

        voicesAdapter = VoicesAdapter(
            voiceEffects,
            spanCount = 3,
            onParentClick = { effect ->
                // parent item click
                Toast.makeText(requireContext(), "Parent clicked: ${effect.name}", Toast.LENGTH_SHORT).show()
            },
       this@VoicesFragment
        )

        val glm = GridLayoutManager(requireContext(), spanCount)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (voicesAdapter.getItemViewType(position) == VoicesAdapter.VIEW_TYPE_EXPANDED) {
                    spanCount
                } else 1
            }
        }

        binding?.rvVoice?.layoutManager = glm
        binding?.rvVoice?.adapter = voicesAdapter
    }


    private var mActivity: FragmentActivity? = null
          override fun onAttach(context: Context) {
             super.onAttach(context)
             mActivity = requireActivity()
              callback = parentFragment as? VoiceFragmentCallback
         }

         override fun onDetach() {
             super.onDetach()
             mActivity = null
         }

    override fun onAdjust(position: Int, speedProgress : Int, pitchProgress : Int) {
        callback?.onValuesAdjusted(position, speedProgress, pitchProgress)
    }

    override fun onItemClick(
        position: Int,
        item: VoiceEffect
    ) {
       callback?.onItemClick(position, item)
    }

    private var callback : VoiceFragmentCallback? = null

    interface VoiceFragmentCallback{
        fun onValuesAdjusted(position: Int, speedProgress : Int, pitchProgress : Int)

        fun onItemClick(position: Int, item : VoiceEffect)
    }
}