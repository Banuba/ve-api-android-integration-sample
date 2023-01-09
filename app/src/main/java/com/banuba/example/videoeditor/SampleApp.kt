package com.banuba.example.videoeditor

import android.app.Application
import com.banuba.sdk.token.storage.license.BanubaVideoEditor

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        BanubaVideoEditorSDK().initialize(this@SampleApp)

        BanubaVideoEditor.initialize(getString(R.string.banuba_token))
    }
}