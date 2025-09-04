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
            VoiceEffect(0,"Default", R.drawable.default_,1.0f,1.0f),
            VoiceEffect(1,"Girl", R.drawable.default_, 1.20f, 1.00f),
            VoiceEffect(2,"Boy", R.drawable.default_, 1.10f, 1.00f),
            VoiceEffect(3,"Child", R.drawable.default_,1.50f, 1.08f),
            VoiceEffect(4,"Old", R.drawable.default_,0.80f, 0.95f),
            VoiceEffect(5,"Soprano", R.drawable.default_,1.70f, 1.05f),
            VoiceEffect(6,"Basso", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(7,"Reverse", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(8,"Small Robot", R.drawable.default_,1.40f, 1.15f),
            VoiceEffect(9,"Cave", R.drawable.default_,0.90f, 0.95f),
            VoiceEffect(10,"Squirrel", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(11,"Grim Reaper", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(12,"Nervous", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(13,"Small Alien", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(14,"Villain", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(15,"Ghost", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(16,"Parody", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(17,"Underwater", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(18,"Drunk", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(19,"Phone", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(20,"Motorcycle", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(21,"Helium Gas", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(22,"Storm Wind", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(23,"Scared", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(24,"Megaphone", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(25,"Monster", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(26,"Canyon", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(27,"Big Robot", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(28,"Chipmunk", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(29,"Zombie", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(30,"Auto-wah", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(31,"Big Alien", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(32,"Sulfur Gas", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(33,"Factory", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(34,"Alien", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(35,"Volume Envelope", R.drawable.default_,0.65f, 0.95f),

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