package com.voicechanger.funnysound.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.VoiceEffect


const val DATA_STORE_NAME = "VOICE-CHANGER"

val IS_ONBOARD = booleanPreferencesKey("onboarding-check")

enum class EffectType {
    NONE,
    ECHO,
    REVERB,
    ROBOT,
    UNDERWATER,
    PHONE,
    DISTORTION
}


 fun getAllVoiceEffects() : ArrayList<VoiceEffect> {
    val voiceEffects = arrayListOf<VoiceEffect>()
    voiceEffects.addAll(
        listOf(
            VoiceEffect(1, "Bird", R.drawable.im_car),
            VoiceEffect(2, "Car Passing", R.drawable.im_car),
            VoiceEffect(3, "Cat", R.drawable.im_car),
            VoiceEffect(4, "Child", R.drawable.im_car),
            VoiceEffect(5, "Dog", R.drawable.im_car),
            VoiceEffect(6, "Door", R.drawable.im_car),
            VoiceEffect(7, "Fart", R.drawable.im_car),
            VoiceEffect(8, "Fireworks", R.drawable.im_car),
            VoiceEffect(9, "FX", R.drawable.im_car),
            VoiceEffect(10, "Gunshot", R.drawable.im_car),
            VoiceEffect(11, "Incoming Call", R.drawable.im_car),
            VoiceEffect(12, "Mosquito", R.drawable.im_car),
            VoiceEffect(13, "Ocean", R.drawable.im_car),
            VoiceEffect(14, "Police Siren", R.drawable.im_car),
            VoiceEffect(15, "Rain", R.drawable.im_car),
            VoiceEffect(16, "Siren", R.drawable.im_car),
            VoiceEffect(17, "Summer Night Loop", R.drawable.im_car),
            VoiceEffect(18, "Thunder", R.drawable.im_car),
            VoiceEffect(19, "Tiger", R.drawable.im_car),
            VoiceEffect(20, "Alarm", R.drawable.im_car),
        )
    )
    return voiceEffects
}