package com.banuba.example.videoeditor

import android.app.Application

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        BanubaVideoEditorSDK().initialize(this@SampleApp)
    }
}