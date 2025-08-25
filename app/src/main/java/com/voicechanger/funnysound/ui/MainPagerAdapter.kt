package com.voicechanger.funnysound.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.voicechanger.funnysound.ui.home.HomeFragment

class MainPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragmentList = arrayListOf<Fragment>()


    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> HomeFragment()
            2 -> HomeFragment()

            else -> HomeFragment()
        }
    }
    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
    }

}