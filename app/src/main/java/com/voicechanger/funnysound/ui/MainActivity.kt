package com.voicechanger.funnysound.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.ActivityMainBinding
import com.voicechanger.funnysound.utils.changeStatusBarColor
import com.voicechanger.funnysound.utils.hideNavigationBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private  var binding: ActivityMainBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideNavigationBar()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        fixBottomNavInsets()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        changeStatusBarColor(R.color.bg_color, this@MainActivity, false)

    }




    private fun fixBottomNavInsets() {
        val bottomNav = binding?.bottomNavView
        bottomNav?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(left = sys.left, right = sys.right, bottom = 0)
                insets
            }
        }
    }

}