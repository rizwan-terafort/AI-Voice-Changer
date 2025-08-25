package com.voicechanger.funnysound

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoiceChangerApplication : Application() {


    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null


        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: VoiceChangerApplication
        fun getInstance(): VoiceChangerApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        instance = this
    }




}