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
import com.voicechanger.funnysound.databinding.FragmentPrankSoundBinding
import com.voicechanger.funnysound.ui.home.PrankSoundAdapter
import com.voicechanger.funnysound.utils.AppUtils

class PrankSoundsFragment : Fragment() {

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

            handleBackPress(activity)

            val adapter = PrankSoundAdapter(activity)
            binding?.rvPrankSound?.adapter = adapter
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
}