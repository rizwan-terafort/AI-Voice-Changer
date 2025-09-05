package com.voicechanger.funnysound.ui.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.databinding.FragmentLanguageBinding
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.ui.MainActivity
import com.voicechanger.funnysound.ui.onbaording.OnboardingActivity
import com.voicechanger.funnysound.utils.AppUtils

class LanguageFragment : Fragment(), LanguageSelectionAdapter.LanguageSelectionClickListener {
    private var binding : FragmentLanguageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->

           if (mActivity is MainActivity){
               AppUtils.getMain(activity).hideToolbar()
               AppUtils.getMain(activity).hideBottomNavigationView()
           }

            val languageList = arrayListOf<LanguageModel>()
            languageList.add(LanguageModel(1,"en","English (Auto)", ContextCompat.getDrawable(activity,R.drawable.flag_us),true))
            languageList.add(LanguageModel(2,"jp","Japanese", ContextCompat.getDrawable(activity,R.drawable.flag_japan),false))
            languageList.add(LanguageModel(3,"hi","Hindi", ContextCompat.getDrawable(activity,R.drawable.flag_india),false))
            languageList.add(LanguageModel(4,"in","Indonesian", ContextCompat.getDrawable(activity,R.drawable.flag_indonesia),false))
            languageList.add(LanguageModel(5,"sp","Spanish", ContextCompat.getDrawable(activity,R.drawable.flag_spain),false))

            val adapter = LanguageSelectionAdapter(languageList,this)
            binding?.rvLanguage?.adapter = adapter

            handleBackPress(activity)
            binding?.back?.setOnClickListener {
                goBack(activity)
            }
            binding?.btnDone?.setOnClickListener {
                startActivity(Intent(mActivity, OnboardingActivity::class.java))
                mActivity?.finish()
            }
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

    override fun onLanguageClick(language: LanguageModel?) {
        Toast.makeText(requireActivity(), language?.name, Toast.LENGTH_SHORT).show()
    }

    private fun goBack(activity: FragmentActivity){
        findNavController().popBackStack()
        AppUtils.getMain(activity).showToolbar()
        AppUtils.getMain(activity).showBottomNavigationView()
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
}