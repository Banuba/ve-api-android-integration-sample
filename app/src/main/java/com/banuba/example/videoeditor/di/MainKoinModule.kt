package com.banuba.example.videoeditor.di

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.banuba.example.videoeditor.R
import com.banuba.example.videoeditor.editor.EditorViewModel
import com.banuba.example.videoeditor.export.CustomExportParamsProvider
import com.banuba.example.videoeditor.export.CustomPublishManager
import com.banuba.example.videoeditor.export.StubImageLoader
import com.banuba.sdk.core.domain.ImageLoader
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.export.data.ExportFlowManager
import com.banuba.sdk.export.data.ExportParamsProvider
import com.banuba.sdk.export.data.ForegroundExportFlowManager
import com.banuba.sdk.token.storage.provider.TokenProvider
import com.banuba.sdk.ve.data.PublishManager
import com.banuba.sdk.ve.effects.WatermarkProvider
import com.banuba.sdk.ve.media.VideoGalleryResourceValidator
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

class MainKoinModule {

    val module = module {

        viewModel {
            EditorViewModel(
                appContext = androidContext(),
                videoValidator = VideoGalleryResourceValidator(
                    context = androidContext()
                ),
                videoPlayer = get(),
                exportFlowManager = get(),
                aspectRatioProvider = get()
            )
        }

        single<TokenProvider>(named("banubaTokenProvider"), override = true) {
            object : TokenProvider {
                override fun getToken(): String =
                    androidContext().getString(R.string.banuba_token)
            }
        }

        single<ExportFlowManager>(override = true) {
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

        factory<ExportParamsProvider>(override = true) {
            CustomExportParamsProvider(
                exportDir = get(named("exportDir")),
                mediaFileNameHelper = get(),
                watermarkBuilder = get(),
                exportAudioProvider = get()
            )
        }

        single<WatermarkProvider>(override = true) {
            object : WatermarkProvider {
                override fun getWatermarkBitmap(): Bitmap? = BitmapFactory.decodeResource(
                    androidContext().resources,
                    com.banuba.sdk.ve.R.drawable.df_fsfw
                )
            }
        }

        single<PublishManager>(override = true) {
            CustomPublishManager(
                context = androidContext(),
                albumName = "Banuba Video Editor",
                mediaFileNameHelper = get(),
                dispatcher = Dispatchers.IO
            )
        }

        single<ImageLoader>(override = true) {
            StubImageLoader()
        }

        single(named("videoDurationExtractor")) {
            DurationExtractor(video = true)
        }
    }
}
