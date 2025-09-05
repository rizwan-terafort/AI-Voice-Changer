package com.voicechanger.funnysound.ui.language

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.ActivityLanguageBinding
import com.voicechanger.funnysound.utils.changeStatusBarColor
import com.voicechanger.funnysound.utils.hideNavigationBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : AppCompatActivity() {
    private var binding: ActivityLanguageBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //    enableEdgeToEdge()
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideNavigationBar()
        changeStatusBarColor(R.color.bg_color, this@LanguageActivity, false)

        val fragment = LanguageFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.nav_host_language, fragment, fragment.javaClass.name)
        ft.commitAllowingStateLoss()

    }


}