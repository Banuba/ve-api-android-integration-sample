package com.banuba.example.videoeditor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.banuba.example.videoeditor.editor.EditorViewModel
import com.banuba.example.videoeditor.export.CustomExportParamsProvider
import com.banuba.example.videoeditor.export.CustomPublishManager
import com.banuba.example.videoeditor.export.EnableExportAudioProvider
import com.banuba.example.videoeditor.export.StubImageLoader
import com.banuba.sdk.core.domain.ImageLoader
import com.banuba.sdk.export.data.ExportFlowManager
import com.banuba.sdk.export.data.ExportParamsProvider
import com.banuba.sdk.export.data.ForegroundExportFlowManager
import com.banuba.sdk.export.data.PublishManager
import com.banuba.sdk.export.di.VeExportKoinModule
import com.banuba.sdk.playback.di.VePlaybackSdkKoinModule
import com.banuba.sdk.token.storage.di.TokenStorageKoinModule
import com.banuba.sdk.token.storage.provider.TokenProvider
import com.banuba.sdk.ve.di.VeSdkKoinModule
import com.banuba.sdk.ve.effects.watermark.WatermarkProvider
import com.banuba.sdk.ve.media.VideoGalleryResourceValidator
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class BanubaVideoEditorSDK {
    fun initialize(application: Application) {
        startKoin {
            androidContext(application)
            allowOverride(true)

            modules(
                VeSdkKoinModule().module,
                VeExportKoinModule().module,
                VePlaybackSdkKoinModule().module,
                TokenStorageKoinModule().module,
                VideoEditorApiModule().module
            )
        }
    }
}

private class VideoEditorApiModule {

    val module = module {

        viewModel {
            EditorViewModel(
                appContext = androidApplication(),
                videoValidator = VideoGalleryResourceValidator(
                    context = androidContext()
                ),
                videoPlayer = get(),
                exportFlowManager = get(),
                aspectRatioProvider = get()
            )
        }

        single<TokenProvider>(named("banubaTokenProvider")) {
            object : TokenProvider {
                override fun getToken(): String =
                    androidContext().getString(R.string.banuba_token)
            }
        }

        single<ExportFlowManager> {
            ForegroundExportFlowManager(
                exportDataProvider = get(),
                sessionParamsProvider = get(),
                exportSessionHelper = get(),
                exportDir = get(named("exportDir")),
                publishManager = get(),
                errorParser = get(),
                mediaFileNameHelper = get(),
                exportBundleProvider = get()
            )
        }

        factory<ExportParamsProvider> {
            CustomExportParamsProvider(
                exportDir = get(named("exportDir")),
                mediaFileNameHelper = get(),
                watermarkBuilder = get(),
                exportAudioProvider = get()
            )
        }

        single<WatermarkProvider> {
            object : WatermarkProvider {
                override fun getWatermarkBitmap(): Bitmap? = BitmapFactory.decodeResource(
                    androidContext().resources,
                    com.banuba.sdk.ve.R.drawable.df_fsfw
                )
            }
        }

        single<PublishManager> {
            CustomPublishManager(
                context = androidContext(),
                albumName = "Banuba Video Editor",
                mediaFileNameHelper = get(),
                dispatcher = Dispatchers.IO
            )
        }

        single<ImageLoader> {
            StubImageLoader()
        }

        single<EnableExportAudioProvider> {
            object : EnableExportAudioProvider {
                override var isEnable: Boolean = false
            }
        }
    }
}