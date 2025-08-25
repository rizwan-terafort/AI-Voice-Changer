package com.voicechanger.funnysound.utils

import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.hideNavigationBar(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.hide(android.view.WindowInsets.Type.navigationBars())
        window.insetsController?.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }else{
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    }
}



fun changeStatusBarColor(@ColorRes colorRes: Int, context: FragmentActivity?, darkIcons: Boolean = false) {
    context?.let { activity ->
        val window = activity.window
        val color = ContextCompat.getColor(activity, colorRes)
        // Set status bar background color
        window.statusBarColor = color
        // Handle icon/text color
        val wic = WindowInsetsControllerCompat(window, window.decorView)
        wic.isAppearanceLightStatusBars = darkIcons
    }
}


