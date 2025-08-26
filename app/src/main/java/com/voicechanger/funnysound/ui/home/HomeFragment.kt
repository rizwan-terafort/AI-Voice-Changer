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
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

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
            val adapter = PrankSoundAdapter(activity)
            binding?.rvPrankSound?.adapter = adapter

            val adapter2 = PrankSoundAdapter(activity)
            binding?.rvVoiceEffects?.adapter = adapter2

            binding?.recordButton?.setOnClickListener {
               goToRecorder()
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


}