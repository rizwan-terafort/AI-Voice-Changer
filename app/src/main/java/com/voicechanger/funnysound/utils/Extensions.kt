package com.voicechanger.funnysound.utils

import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.hideNavigationBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.hide(android.view.WindowInsets.Type.navigationBars())
        window.insetsController?.systemBarsBehavior =
            android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    }
}


fun changeStatusBarColor(
    @ColorRes colorRes: Int,
    context: FragmentActivity?,
    darkIcons: Boolean = false
) {
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

fun Int.toFloatValue(): Float {
    return if (this <= 10) {
        // First group includes 1.0 → 1.9 (10 values)
        1.0f + (this - 1) * 0.1f
    } else {
        // After that, each group has only 9 values (skipping .0)
        val group = (this - 11) / 9 + 2   // integer part (2, 3, 4…)
        val step = (this - 11) % 9 + 1    // decimal part (1…9)
        group + step * 0.1f
    }
}

fun Float.toIntValue(): Int {
    val rounded = (this * 10 + 0.01f).toInt() // handle precision (e.g. 3.8999 → 39)

    return if (this in 1.0f..1.9f) {
        // First group
        (this * 10).toInt() - 9
    } else {
        val group = rounded / 10
        val step = rounded % 10
        11 + (group - 2) * 9 + (step - 1)
    }
}


