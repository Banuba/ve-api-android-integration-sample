package com.banuba.example.videoeditor.camera

import android.net.Uri

data class Effect(
    val uri: Uri,
    val name: String,
    val previewImagePath: Uri
)
