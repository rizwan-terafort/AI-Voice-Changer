package com.voicechanger.funnysound.data

import com.voicechanger.funnysound.common.EffectType

data class VoiceEffect(
    val id : Int = 0,
    val name: String = "",
    val iconResId: Int = 0,
    val pitch : Float = 0.0f,
    val speed : Float = 0.0f,
    val effectType: EffectType = EffectType.NONE
)

