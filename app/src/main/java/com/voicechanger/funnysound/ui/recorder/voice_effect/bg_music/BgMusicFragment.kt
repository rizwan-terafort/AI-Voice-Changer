package com.voicechanger.funnysound.ui.recorder.voice_effect.bg_music

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
import com.voicechanger.funnysound.databinding.FragmentBgMusicBinding
import com.voicechanger.funnysound.databinding.FragmentVoicesBinding
import com.voicechanger.funnysound.ui.recorder.voice_effect.SpeedAdjustListener
import com.voicechanger.funnysound.ui.recorder.voice_effect.VoicesAdapter
import com.voicechanger.funnysound.ui.recorder.voice_effect.VoicesFragment

class BgMusicFragment : Fragment(), BgMusicVolumeChangeListener {
    private var binding : FragmentBgMusicBinding? = null

    private val voiceEffects = mutableListOf<VoiceEffect>()



    private lateinit var adapter:  BgMusicAdapter

    private val spanCount = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBgMusicBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVoiceEffects()
        setupRecyclerView()
    }

    private fun setupVoiceEffects() {
        voiceEffects.addAll(listOf(
            VoiceEffect(0,"Default", R.drawable.ic_mic_new,50f,50f),
            VoiceEffect(1,"Girl", R.drawable.ic_mic_new, 50f, 50f),
            VoiceEffect(2,"Boy", R.drawable.ic_mic_new, 50f, 50f),
        ))
    }

    private fun setupRecyclerView() {

        adapter = BgMusicAdapter(
            voiceEffects,
            spanCount = 3,
            onParentClick = { effect ->
                // parent item click
                Toast.makeText(requireContext(), "Parent clicked: ${effect.name}", Toast.LENGTH_SHORT).show()
            },
            this@BgMusicFragment
        )

        val glm = GridLayoutManager(requireContext(), spanCount)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == VoicesAdapter.VIEW_TYPE_EXPANDED) {
                    spanCount
                } else 1
            }
        }

        binding?.rvBgMusic?.layoutManager = glm
        binding?.rvBgMusic?.adapter = adapter
    }


    private var mActivity: FragmentActivity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
        callback = parentFragment as? BgMusicVolumeCallback
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }


    private var callback : BgMusicVolumeCallback? = null
    override fun onVolumeChanged(
        value: Int,
        item: VoiceEffect
    ) {
       callback?.onVolumeChanged(value, item)
    }

    interface BgMusicVolumeCallback{
        fun onVolumeChanged(value : Int, item : VoiceEffect)
    }
}