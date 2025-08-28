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
            VoiceEffect("Default", R.drawable.ic_mic_new, "Default voice effect with no modifications", R.drawable.ic_mic_new),
            VoiceEffect("Girl", R.drawable.ic_mic_new, "High-pitched voice effect for female voice", R.drawable.ic_mic_new),
            VoiceEffect("Teacher", R.drawable.ic_mic_new, "Clear and authoritative voice effect", R.drawable.ic_mic_new),
            VoiceEffect("Boy", R.drawable.ic_mic_new, "Young male voice effect", R.drawable.ic_mic_new),
            VoiceEffect("Old Man", R.drawable.ic_mic_new, "Deep and aged voice effect", R.drawable.ic_mic_new),
            VoiceEffect("Tiger", R.drawable.ic_mic_new, "Growling animal voice effect", R.drawable.ic_mic_new),
            VoiceEffect("Robot", R.drawable.ic_mic_new, "Mechanical and robotic voice effect", R.drawable.ic_mic_new),
            VoiceEffect("Alien", R.drawable.ic_mic_new, "Otherworldly alien voice effect", R.drawable.ic_mic_new),
            VoiceEffect("Chipmunk", R.drawable.ic_mic_new, "High-speed chipmunk voice effect", R.drawable.ic_mic_new)
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

    override fun onAdjust(position: Int, isSpeed: Boolean, progress: Int) {
        callback?.onValuesAdjusted(position, isSpeed, progress)
    }

    private var callback : VoiceFragmentCallback? = null

    interface VoiceFragmentCallback{
        fun onValuesAdjusted(position: Int, isSpeed : Boolean, progress : Int)
    }
}