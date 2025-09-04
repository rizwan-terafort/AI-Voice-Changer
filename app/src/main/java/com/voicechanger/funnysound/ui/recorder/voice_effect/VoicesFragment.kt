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
import com.voicechanger.funnysound.utils.EffectType

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
            //   VoiceEffect(7,"Reverse", R.drawable.default_,0.65f, 0.95f),

            VoiceEffect(
                id = 7,
                name = "Small Robot",
                iconResId = R.drawable.default_,
                pitch = 1.30f,   // thoda high pitch robotic feel
                speed = 0.90f    // halki si slow for mechanical effect
            ),

            VoiceEffect(
                id = 8,
                name = "Cave",
                iconResId = R.drawable.default_,
                pitch = 1.00f,   // normal pitch
                speed = 0.80f    ,
                effectType = EffectType.REVERB
            ),

            VoiceEffect(
                id = 9,
                name = "Squirrel",
                iconResId = R.drawable.default_,
                pitch = 1.80f,   // high pitch chipmunk style
                speed = 1.20f    // fast bolne ka effect
            ),

            VoiceEffect(
                id = 10,
                name = "Grim Reaper",
                iconResId = R.drawable.default_,
                pitch = 0.70f,   // low, dark tone
                speed = 0.85f    // thoda drag feel
            ),

            VoiceEffect(
                id = 11,
                name = "Nervous",
                iconResId = R.drawable.default_,
                pitch = 1.40f,   // slightly high pitch
                speed = 1.30f    // fast, nervous speaking effect
            ),
            VoiceEffect(
                id = 12,
                name = "Ghost",
                iconResId = R.drawable.default_,
                pitch = 0.5f,   // thoda low, hollow feeling
                speed = 0.7f    // slow, dragging effect
            ),

            VoiceEffect(13,"Small Alien", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(14,"Villain", R.drawable.default_,0.65f, 0.95f),
            //   VoiceEffect(15,"Ghost", R.drawable.default_,0.65f, 0.95f),
            VoiceEffect(16,"Parody", R.drawable.default_,0.65f, 0.95f),


            VoiceEffect(
                id = 17,
                name = "Underwater",
                iconResId = R.drawable.default_,
                pitch = 0.9f,
                speed = 0.9f,
                effectType = EffectType.UNDERWATER
            ),

            VoiceEffect(
                18,
                "Drunk",
                R.drawable.default_,
                pitch = 0.7f,   // low, heavy
                speed = 0.6f    // slow, dragging
            ),

            VoiceEffect(
                19,
                "Phone",
                R.drawable.default_,
                pitch = 1.2f,   // thoda high, tinny
                speed = 1.0f    // normal
            ),

            VoiceEffect(
                20,
                "Motorcycle",
                R.drawable.default_,
                pitch = 0.8f,   // gritty, low
                speed = 1.3f    // fast vibration effect
            ),

            VoiceEffect(
                21,
                "Helium Gas",
                R.drawable.default_,
                pitch = 1.8f,   // super high pitch
                speed = 1.2f    // thoda fast
            ),

            VoiceEffect(
                22,
                "Storm Wind",
                R.drawable.default_,
                pitch = 0.9f,   // slightly low
                speed = 0.8f    // stretched, windy effect
            ),

            VoiceEffect(
                23,
                "Scared",
                R.drawable.default_,
                pitch = 1.4f,   // sharp, nervous
                speed = 1.1f    // thoda fast
            ),

            VoiceEffect(
                24,
                "Megaphone",
                R.drawable.default_,
                pitch = 1.1f,   // thoda sharp
                speed = 1.0f    // normal, echo/reverb sath lagana zaroori
            ),

            VoiceEffect(
                25,
                "Monster",
                R.drawable.default_,
                pitch = 0.6f,   // very deep
                speed = 0.8f    // slow, heavy
            ),

            VoiceEffect(
                26,
                "Canyon",
                R.drawable.default_,
                pitch = 1.0f,   // normal pitch
                speed = 0.9f    // thoda drag
                // + echo/reverb zaroori h
            ),

            VoiceEffect(
                27,
                "Big Robot",
                R.drawable.default_,
                pitch = 0.8f,   // robotic, low
                speed = 1.0f    // steady
            ),

            VoiceEffect(
                28,
                "Chipmunk",
                R.drawable.default_,
                pitch = 1.9f,   // very high
                speed = 1.3f    // fast
            ),

            VoiceEffect(
                29,
                "Zombie",
                R.drawable.default_,
                pitch = 0.7f,   // deep, groaning
                speed = 0.7f    // slow, dragging
            ),

            VoiceEffect(
                30,
                "Auto-wah",
                R.drawable.default_,
                pitch = 1.0f,
                speed = 1.0f
                // lekin isme wah-wah filter lagana chahiye, pitch/speed se alone kaam nahi banega
            ),

            VoiceEffect(
                31,
                "Big Alien",
                R.drawable.default_,
                pitch = 0.6f,   // deep & strange
                speed = 1.2f    // thoda fast distortion feel
            ),

            VoiceEffect(
                32,
                "Sulfur Gas",
                R.drawable.default_,
                pitch = 1.6f,   // sharp toxic
                speed = 1.1f
            ),

            VoiceEffect(
                33,
                "Factory",
                R.drawable.default_,
                pitch = 0.9f,   // industrial tone
                speed = 0.8f
            ),

            VoiceEffect(
                34,
                "Alien",
                R.drawable.default_,
                pitch = 1.3f,   // unusual, sharp
                speed = 0.9f
            ),

            VoiceEffect(
                35,
                "Volume Envelope",
                R.drawable.default_,
                pitch = 1.0f,
                speed = 1.0f
                // yaha pitch/speed normal rakho, lekin ADSR volume modulation lagani hoti hai
            )

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