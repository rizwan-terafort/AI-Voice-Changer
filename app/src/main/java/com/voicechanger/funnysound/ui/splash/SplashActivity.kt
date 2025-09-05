package com.voicechanger.funnysound.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.common.DataStoreManager
import com.voicechanger.funnysound.common.IS_ONBOARD
import com.voicechanger.funnysound.databinding.ActivityMainBinding
import com.voicechanger.funnysound.databinding.ActivitySplashBinding
import com.voicechanger.funnysound.ui.MainActivity
import com.voicechanger.funnysound.ui.language.LanguageActivity
import com.voicechanger.funnysound.utils.changeStatusBarColor
import com.voicechanger.funnysound.utils.hideNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    private var isFirst = false

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        changeStatusBarColor(R.color.bg_color, this@SplashActivity, false)
        hideNavigationBar()

        dataStoreManager.readDataStoreValue(IS_ONBOARD, false) {
            isFirst = this
        }

        binding?.loadingBar?.progress = 0
        lifecycleScope.launch {
            startLoadingProgress()
            binding?.loadingBar?.progress = 100
           navigateNext()
        }


    }

    private fun navigateNext(){
        if (!isFirst){
            startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
        }else{
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
        finish()
    }

    private suspend fun startLoadingProgress() {
        var progress = 0
        while (progress < 99) { // Stop at 99% progress
            delay(50) // Adjust this value to change the speed of the progress bar
            progress += 1
            binding?.loadingBar?.progress = progress
        }

    }
}