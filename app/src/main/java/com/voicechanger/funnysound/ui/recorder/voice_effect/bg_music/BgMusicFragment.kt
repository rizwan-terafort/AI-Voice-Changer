package com.voicechanger.funnysound.ui.recorder.voice_effect.bg_music

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
import com.voicechanger.funnysound.databinding.FragmentBgMusicBinding
import com.voicechanger.funnysound.ui.recorder.voice_effect.VoicesAdapter

class BgMusicFragment : Fragment(), BgMusicVolumeChangeListener {
    private var binding: FragmentBgMusicBinding? = null

    private val voiceEffects = mutableListOf<VoiceEffect>()


    private lateinit var adapter: BgMusicAdapter

    private val spanCount = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBgMusicBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVoiceEffects()
        setupRecyclerView()
    }

    private fun setupVoiceEffects() {
        voiceEffects.addAll(
            listOf(
                VoiceEffect(0, "Default", R.drawable.default_, 50f, 50f),
                VoiceEffect(1, "Bird", R.drawable.default_, 50f, 50f),
                VoiceEffect(2, "Vehicle", R.drawable.default_, 50f, 50f),
                VoiceEffect(3, "Cat", R.drawable.default_, 50f, 50f),
                VoiceEffect(4, "Child", R.drawable.default_, 50f, 50f),
                VoiceEffect(5, "Dog", R.drawable.default_, 50f, 50f),
                VoiceEffect(6, "Door", R.drawable.default_, 50f, 50f),
                VoiceEffect(7, "Fart", R.drawable.default_, 50f, 50f),
                VoiceEffect(8, "Fire Work", R.drawable.default_, 50f, 50f),
                VoiceEffect(9, "FX", R.drawable.default_, 50f, 50f),
                VoiceEffect(10, "Gun Shot", R.drawable.default_, 50f, 50f),
                VoiceEffect(11, "Ringtone", R.drawable.default_, 50f, 50f),
                VoiceEffect(12, "Mosquito", R.drawable.default_, 50f, 50f),
                VoiceEffect(13, "Ocean", R.drawable.default_, 50f, 50f),
                VoiceEffect(14, "Police Siren", R.drawable.default_, 50f, 50f),
                VoiceEffect(15, "Rain", R.drawable.default_, 50f, 50f),
                VoiceEffect(16, "Siren", R.drawable.default_, 50f, 50f),
                VoiceEffect(17, "Summer Loop", R.drawable.default_, 50f, 50f),
                VoiceEffect(18, "Thunder", R.drawable.default_, 50f, 50f),
                VoiceEffect(19, "Tiger", R.drawable.default_, 50f, 50f),
                VoiceEffect(20, "Alarm", R.drawable.default_, 50f, 50f),
            )
        )
    }

    private fun setupRecyclerView() {

        adapter = BgMusicAdapter(
            voiceEffects,
            spanCount = 3,
            onParentClick = { effect ->
                // parent item click
              //  Toast.makeText(requireContext(), "Parent clicked: ${effect.name}", Toast.LENGTH_SHORT).show()
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


    private var callback: BgMusicVolumeCallback? = null
    override fun onBgMusicItemClick(position: Int) {
        callback?.onMusicItemClick(position)
    }

    override fun onMusicVolumeChanged(
        progress: Int,
        item: VoiceEffect
    ) {
        callback?.onMusicVolumeChanged(progress, item)
    }

    interface BgMusicVolumeCallback {
        fun onMusicVolumeChanged(progress: Int, item: VoiceEffect)

        fun onMusicItemClick(position: Int)
    }
}