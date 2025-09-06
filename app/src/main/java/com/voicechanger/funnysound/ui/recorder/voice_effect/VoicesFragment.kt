package com.voicechanger.funnysound.ui.recorder.voice_effect

import android.content.Context
import android.os.Bundle
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
import com.voicechanger.funnysound.common.EffectType
import com.voicechanger.funnysound.common.getAllVoiceEffects

class VoicesFragment : Fragment(), SpeedAdjustListener {
    private var binding : FragmentVoicesBinding? = null

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
        setupRecyclerView()
    }


    private fun setupRecyclerView() {

        voicesAdapter = VoicesAdapter(
            getAllVoiceEffects(),
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