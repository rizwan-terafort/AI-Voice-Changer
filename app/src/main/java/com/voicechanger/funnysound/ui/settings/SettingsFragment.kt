package com.voicechanger.funnysound.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.FragmentSettingsBinding
import com.voicechanger.funnysound.ui.MainFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class SettingsFragment : Fragment(), SettingsClickListener {
    private var binding: FragmentSettingsBinding? = null
    private var settingsAdapter: SettingsAdapter? = null
    private var mActivity: FragmentActivity? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setClickListeners()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->
            settingsAdapter = SettingsAdapter(activity, arrayListOf(), this)
            val layoutManager = LinearLayoutManager(activity)
            binding?.rvSettings?.layoutManager = layoutManager
            binding?.rvSettings?.adapter = settingsAdapter
            lifecycleScope.launch {
                getSettingsFlow().collectLatest { list ->
                    withContext(Dispatchers.Main) {
                        settingsAdapter?.updateList(list)

                    }
                }
            }
        }
    }

    private fun setClickListeners() {


    }

    private fun getDrawable(int: Int): Drawable? {
        return mActivity?.let { ContextCompat.getDrawable(it, int) }
    }

    private fun getSettingsFlow(): Flow<List<ModelSettings>> = flow {
        val list = arrayListOf<ModelSettings>()
        list.add(ModelSettings(99,getString(R.string.general), true))
        list.add(
            ModelSettings(
                1,
                getString(R.string.change_language),
                false,
                icon = getDrawable(R.drawable.ic_change_language_24),
                background = getDrawable(R.drawable.bg_setting_top)
            )
        )
        list.add(
            ModelSettings(
                2,
                getString(R.string.rate_us),
                false,
                icon = getDrawable(R.drawable.ic_rate_us_24),
                background = getDrawable(R.drawable.bg_setting_middle)
            )
        )


        list.add(
            ModelSettings(
                3,
                getString(R.string.share_the_app),
                false,
                icon = getDrawable(R.drawable.ic_share_24_),
                background = getDrawable(R.drawable.bg_setting_middle),
            )

        )
        list.add(
            ModelSettings(
                4,
                getString(R.string.privacy_policy),
                false,
                icon = getDrawable(R.drawable.ic_privacy_policy_24),
                background = getDrawable(R.drawable.bg_setting_middle)
            )
        )
        list.add(
            ModelSettings(
                5, getString(R.string.terms_of_use),
                false,
                icon = getDrawable(R.drawable.ic_term_of_use_24),
                background = getDrawable(R.drawable.bg_setting_bottom)
            )
        )


        emit(list)
    }

    override fun onSettingItemClick(id: Int) {
        when (id) {
            1 -> {
                findNavController().navigate(MainFragmentDirections.actionSettingsToLanguage())
            }

        }
    }

    private fun goToCancelSubscription() {
        mActivity?.let { activity ->
            try {
                val packageName = activity.packageName
                val uri =
                    Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun deleteDir(dir: File?): Boolean {
        try {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
            return dir?.delete() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
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

    private fun openSocialMedia(link: String) {
        val socialIntent = Uri.parse(link)
        val facebookIntent = Intent(Intent.ACTION_VIEW, socialIntent)
        try {
            if (mActivity != null) {
                if (mActivity?.packageManager?.let { facebookIntent.resolveActivity(it) } != null) {
                    mActivity?.startActivity(facebookIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, socialIntent)
                    mActivity?.startActivity(browserIntent)
                }
            } else {
                // Handle the case where mActivity is null
            }
        } catch (e: Exception) {
            // Handle the exception here (e.g., log it or show an error message)
            e.printStackTrace()
        }
    }
}