package com.voicechanger.funnysound.ui.recorder.voice_effect

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.voicechanger.funnysound.databinding.FragmentVoicesBinding

class VoicesFragment : Fragment() {
    private var binding : FragmentVoicesBinding? = null

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
        val adapter = VoicesAdapter(arrayListOf()){ item->

        }
        binding?.rvVoice?.layoutManager = GridLayoutManager(requireActivity(),3)
        binding?.rvVoice?.adapter = adapter
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
}