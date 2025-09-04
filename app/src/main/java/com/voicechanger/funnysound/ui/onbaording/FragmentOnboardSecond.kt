package com.voicechanger.funnysound.ui.onbaording

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.voicechanger.funnysound.databinding.FragmentOnbaordingFirstBinding
import com.voicechanger.funnysound.databinding.FragmentOnbaordingSecondBinding

class FragmentOnboardSecond : Fragment() {

    private var binding : FragmentOnbaordingSecondBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnbaordingSecondBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnNextOnboarding?.setOnClickListener {
            OnboardingActivity.selectedPosition.value = 2
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
}