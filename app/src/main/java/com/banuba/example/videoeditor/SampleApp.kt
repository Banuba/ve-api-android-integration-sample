package com.banuba.example.videoeditor

import android.app.Application
import android.util.Log
import com.banuba.sdk.core.license.BanubaVideoEditor

class SampleApp : Application() {

    var videoEditor: BanubaVideoEditor? = null

    companion object {
        const val TAG = "BanubaVideoEditor"

        val LICENSE_TOKEN = SET YOUR LICENSE TOKEN

        const val ERR_SDK_NOT_INITIALIZED = "Banuba Video Editor SDK is not initialized: license token is unknown or incorrect.\nPlease check your license token or contact Banuba"
        const val ERR_LICENSE_REVOKED = "License is revoked or expired. Please contact Banuba https://www.banuba.com/faq/kb-tickets/new"
    }

    override fun onCreate() {
        super.onCreate()

        videoEditor = BanubaVideoEditor.initialize(LICENSE_TOKEN)

        if (videoEditor == null) {
            // Token you provided is not correct - empty or truncated
            Log.e(TAG, ERR_SDK_NOT_INITIALIZED)
        } else {
            // Initialize Banuba VE UI SDK
            VideoEditorApiModule().initialize(this@SampleApp)
        }
    }
}