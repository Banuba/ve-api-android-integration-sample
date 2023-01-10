package com.banuba.example.videoeditor

import android.app.Application
import android.util.Log
import com.banuba.sdk.token.storage.license.BanubaVideoEditor

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        BanubaVideoEditorSDK().initialize(this@SampleApp)

        val videoEditorSDK = BanubaVideoEditor.initialize(getString(R.string.banuba_token))

        if (videoEditorSDK == null) {
            Log.e("BanubaVideoEditor", "BanubaVideoEditor initialization error")
        } else {
            videoEditorSDK.getLicenseState { isValid ->
                if (isValid) {
                    Log.d("BanubaVideoEditor", "BanubaVideoEditor token is valid")
                } else {
                    Log.d("BanubaVideoEditor", "BanubaVideoEditor token is not valid")
                }
            }
        }
    }
}