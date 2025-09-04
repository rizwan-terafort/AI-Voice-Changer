package com.voicechanger.funnysound.ui.onbaording

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.voicechanger.funnysound.databinding.FragmentOnbaordingFirstBinding
import com.voicechanger.funnysound.databinding.FragmentOnbaordingSecondBinding
import com.voicechanger.funnysound.databinding.FragmentOnbaordingThirdBinding
import com.voicechanger.funnysound.ui.MainActivity

class FragmentOnboardThird : Fragment() {

    private var binding: FragmentOnbaordingThirdBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnbaordingThirdBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnNextOnboarding?.setOnClickListener {
          //  OnboardingActivity.selectedPosition.value = 3
            startActivity(Intent(requireActivity(), MainActivity::class.java))
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