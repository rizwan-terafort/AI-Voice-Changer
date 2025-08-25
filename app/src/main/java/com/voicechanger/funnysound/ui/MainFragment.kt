package com.voicechanger.funnysound.ui

import com.voicechanger.funnysound.ui.home.HomeFragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.voicechanger.funnysound.databinding.FragmentMainBinding


class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private var mActivity: FragmentActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = this

        return binding?.root
    }

    companion object {
        var selectedItem = MutableLiveData(1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let {

            val adapter = MainPagerAdapter(childFragmentManager, lifecycle)
            adapter.addFragment(HomeFragment())
            adapter.addFragment(HomeFragment())
            adapter.addFragment(HomeFragment())
            binding?.viewPager?.adapter = adapter
            binding?.viewPager?.isUserInputEnabled = false

            selectedItem.observe(viewLifecycleOwner) {
                when (it) {
                    0 -> binding?.viewPager?.currentItem = 0
                    1 -> binding?.viewPager?.currentItem = 1
                    2 -> binding?.viewPager?.currentItem = 2
                }
            }

        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

}