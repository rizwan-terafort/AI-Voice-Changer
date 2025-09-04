package com.voicechanger.funnysound.data

import com.voicechanger.funnysound.utils.EffectType

data class VoiceEffect(
    val id : Int,
    val name: String,
    val iconResId: Int,
    val pitch : Float,
    val speed : Float,
    val effectType: EffectType = EffectType.NONE
)

