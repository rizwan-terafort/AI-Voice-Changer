package com.voicechanger.funnysound.ui.onbaording

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.ActivityMainBinding
import com.voicechanger.funnysound.databinding.ActivityOnboardingBinding
import com.voicechanger.funnysound.ui.MainFragment.Companion.selectedItem
import com.voicechanger.funnysound.ui.MainPagerAdapter
import com.voicechanger.funnysound.ui.home.HomeFragment
import com.voicechanger.funnysound.ui.settings.SettingsFragment
import com.voicechanger.funnysound.utils.changeStatusBarColor
import com.voicechanger.funnysound.utils.hideNavigationBar

class OnboardingActivity : AppCompatActivity() {
    private  var binding: ActivityOnboardingBinding ?= null

    companion object {
        var selectedPosition = MutableLiveData(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideNavigationBar()

        changeStatusBarColor(R.color.bg_color, this@OnboardingActivity, false)
        setViewPagerAdapter()


        selectedPosition.observe(this) {
            when (it) {
                1 -> binding?.vpOnboard?.currentItem = 1
                2 -> binding?.vpOnboard?.currentItem = 2
                //3 -> binding?.vpOnboard?.currentItem = 2
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setViewPagerAdapter() {
        val adapter = OnboardPagerAdapter(supportFragmentManager, lifecycle)

        adapter.addFragment(HomeFragment())
        adapter.addFragment(HomeFragment())
        adapter.addFragment(SettingsFragment())
        binding?.vpOnboard?.adapter = adapter
        binding?.vpOnboard?.isUserInputEnabled = true

            binding?.vpOnboard?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Log.d("checkPositionViewPager","$position")
                    when(position){
                        0-> binding?.vpOnboard?.currentItem = 0
                        1-> binding?.vpOnboard?.currentItem = 1
                        2-> binding?.vpOnboard?.currentItem = 2
                    }
                }
            })

    }


    inner class OnboardPagerAdapter(
        fragmentManager: FragmentManager, lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        private val fragmentList = arrayListOf<Fragment>()


        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FragmentOnboardFirst()
                1 -> FragmentOnboardSecond()
                2 -> FragmentOnboardThird()

                else -> FragmentOnboardFirst()
            }
        }
        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

    }
}