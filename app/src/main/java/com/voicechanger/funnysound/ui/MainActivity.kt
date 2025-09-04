package com.voicechanger.funnysound.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.ActivityMainBinding
import com.voicechanger.funnysound.ui.onbaording.OnboardingActivity
import com.voicechanger.funnysound.utils.changeStatusBarColor
import com.voicechanger.funnysound.utils.hideNavigationBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private  var binding: ActivityMainBinding ?= null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideNavigationBar()
        fixBottomNavInsets()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        changeStatusBarColor(R.color.bg_color, this@MainActivity, false)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding?.bottomNavView?.setOnItemSelectedListener { menuItem->
            when(menuItem.itemId){
                R.id.nav_home ->{
                    binding?.btnPremium?.visibility = View.VISIBLE
                    binding?.title?.text = getString(R.string.ai_voice_changer)
                    MainFragment.selectedItem.value = 0
                }
                R.id.nav_history ->{
                    binding?.btnPremium?.visibility = View.GONE
                    binding?.title?.text = getString(R.string.history)
                    MainFragment.selectedItem.value = 1
                }
                R.id.nav_settings ->{
                    binding?.btnPremium?.visibility = View.GONE
                    binding?.title?.text = getString(R.string.settings)
                    MainFragment.selectedItem.value = 2
                }
            }
            true
        }

        binding?.btnPremium?.setOnClickListener {
            startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
        }
    }


    fun hideToolbar(){
        binding?.actionbarLayout?.visibility = View.GONE
    }

    fun showToolbar(){
        binding?.actionbarLayout?.visibility = View.VISIBLE
    }

    fun hideBottomNavigationView(){
        binding?.bottomNavView?.visibility = View.GONE
    }

    fun showBottomNavigationView(){
        binding?.bottomNavView?.visibility = View.VISIBLE
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
    override fun onBackPressed() {
        val count: Int = supportFragmentManager.backStackEntryCount
        if (count > 0) {
            supportFragmentManager.popBackStack()
        } else {
            when (navController?.currentDestination?.id) {
                R.id.homeFragment -> {

                }
                else -> {

                    super.onBackPressed()
                }
            }
        }
    }

}