package com.voicechanger.funnysound.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.databinding.FragmentHomeBinding
import com.voicechanger.funnysound.ui.MainFragmentDirections
import com.voicechanger.funnysound.utils.PrankSoundClickListener
import dagger.hilt.android.AndroidEntryPoint
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.common.getAllVoiceEffects
import com.voicechanger.funnysound.data.VoiceEffect


@AndroidEntryPoint
class HomeFragment : Fragment(), PrankSoundClickListener {

    private var binding : FragmentHomeBinding? =null


    private var mActivity: FragmentActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity =  requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity?.let { activity->
            val adapter = PrankSoundAdapter(activity, getAllVoiceEffects(),this,)
            binding?.rvPrankSound?.adapter = adapter

            val adapter2 = PrankSoundAdapter(activity, getAllVoiceEffects(),this)
            binding?.rvVoiceEffects?.adapter = adapter2

            binding?.recordButton?.setOnClickListener {
               goToRecorder()
            }
            binding?.writeButton?.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionHomeToTextToVoice())
            }

            binding?.speechToTextCard?.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionHomeToVoiceToText())
            }

            binding?.prankSoundCard?.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionHomeToPrankSound())
            }
            binding?.tvSeeAll1?.setOnClickListener {
                findNavController().navigate(R.id.action_global_to_prank_sounds)
            }
            binding?.tvSeeAll2?.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionHomeToPrankSound())
            }
        }

    }

    private fun goToRecorder(){
        mActivity?.let { activity->

            findNavController().navigate(MainFragmentDirections.actionHomeFragmentToRecorder())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onPrankSoundClick(position: Int, item : VoiceEffect) {
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putBoolean("isFromHome", true)
        findNavController().navigate(R.id.action_global_to_prank_player, bundle)

    }


}