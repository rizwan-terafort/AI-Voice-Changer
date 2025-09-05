package com.voicechanger.funnysound.utils

import com.voicechanger.funnysound.data.VoiceEffect

interface PrankSoundClickListener{
    fun onPrankSoundClick(position: Int, item : VoiceEffect)
}