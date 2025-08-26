package com.voicechanger.funnysound.data

import android.net.Uri

data class Recording(
    val name: String,
    val size : Long,
    val duration: Long,
    val uri: Uri
)
