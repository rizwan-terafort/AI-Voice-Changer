package com.voicechanger.funnysound.common

import androidx.datastore.preferences.core.booleanPreferencesKey


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