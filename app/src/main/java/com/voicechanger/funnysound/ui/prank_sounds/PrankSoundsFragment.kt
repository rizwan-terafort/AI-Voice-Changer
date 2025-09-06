package com.voicechanger.funnysound.ui.prank_sounds

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.common.getAllVoiceEffects
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.databinding.FragmentPrankSoundBinding
import com.voicechanger.funnysound.ui.recorder.voice_effect.VoicesAdapter
import com.voicechanger.funnysound.utils.AppUtils
import com.voicechanger.funnysound.utils.PrankSoundClickListener

class PrankSoundsFragment : Fragment(), PrankSoundClickListener {

    private var binding : FragmentPrankSoundBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPrankSoundBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->

            AppUtils.getMain(activity).hideToolbar()
            AppUtils.getMain(activity).hideBottomNavigationView()

            binding?.closeButton?.setOnClickListener {
                goBack(activity)
            }
            handleBackPress(activity)



            val adapter = PrankSoundsAdapternew(
                activity, getAllVoiceEffects(), this
            )

            val glm = GridLayoutManager(requireContext(), 3)
            glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == VoicesAdapter.VIEW_TYPE_EXPANDED) {
                        3
                    } else 1
                }
            }

            binding?.rvPrankSound?.layoutManager = glm
            binding?.rvPrankSound?.adapter = adapter



            binding?.appBar?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                // Check if the AppBarLayout is fully collapsed
                // Apply fade effect based on the fraction
                val fraction = Math.abs(verticalOffset.toFloat()) / binding?.appBar?.totalScrollRange?.toFloat()!!

                if (fraction > 0.0f && fraction < 1.0f) {
                    // Fade out the subtitle text as the AppBar collapses
                    binding?.subtitleText?.alpha = 1 - fraction // Fading out
                } else if (fraction >= 1.0f) {
                    // Ensure the TextView is fully hidden when collapsed
                    binding?.subtitleText?.alpha = 0f
                } else {
                    // Ensure the TextView is fully visible when expanded
                    binding?.subtitleText?.alpha = 1f
                }
            })
        }
    }



    private var mActivity: FragmentActivity? = null
          override fun onAttach(context: Context) {
             super.onAttach(context)
             mActivity = requireActivity()
         }

         override fun onDetach() {
             super.onDetach()
             mActivity = null
         }


    private fun handleBackPress(activity: FragmentActivity) {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    goBack(activity)
                }

            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, onBackPressedCallback)
    }

    private fun goBack(activity: FragmentActivity){
        findNavController().popBackStack()
        AppUtils.getMain(activity).showToolbar()
        AppUtils.getMain(activity).showBottomNavigationView()
    }

    override fun onPrankSoundClick(
        position: Int,
        item: VoiceEffect
    ) {
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putBoolean("isFromHome", false)
        findNavController().navigate(R.id.action_global_to_prank_player, bundle)
    }


}