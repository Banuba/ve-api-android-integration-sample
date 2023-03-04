package com.banuba.example.videoeditor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import com.banuba.example.videoeditor.export.ExportViewModel
import com.banuba.example.videoeditor.playback.PlaybackViewModel
import com.banuba.example.videoeditor.utils.CustomPublishManager
import com.banuba.sdk.core.VideoResolution
import com.banuba.sdk.core.ext.toPx
import com.banuba.sdk.core.media.MediaFileNameHelper
import com.banuba.sdk.effectplayer.adapter.BanubaEffectPlayerKoinModule
import com.banuba.sdk.export.data.*
import com.banuba.sdk.export.di.VeExportKoinModule
import com.banuba.sdk.playback.di.VePlaybackSdkKoinModule
import com.banuba.sdk.token.storage.di.TokenStorageKoinModule
import com.banuba.sdk.ve.di.VeSdkKoinModule
import com.banuba.sdk.ve.domain.VideoRangeList
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.music.MusicEffect
import com.banuba.sdk.ve.effects.watermark.WatermarkAlignment
import com.banuba.sdk.ve.effects.watermark.WatermarkBuilder
import com.banuba.sdk.ve.effects.watermark.WatermarkProvider
import com.banuba.sdk.ve.ext.withWatermark
import com.banuba.sdk.ve.media.VideoGalleryResourceValidator
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class VideoEditorApiModule {
    fun initialize(application: Application) {
        startKoin {
            androidContext(application)
            allowOverride(true)

            modules(
                VeSdkKoinModule().module,
                VeExportKoinModule().module,
                VePlaybackSdkKoinModule().module,
                TokenStorageKoinModule().module,
                // Module is required for applying Face AR masks
                BanubaEffectPlayerKoinModule().module,
                SampleModule().module
            )
        }
    }
}

private class SampleModule {

    val module = module {
        viewModel {
            ExportViewModel(
                appContext = androidApplication(),
                backgroundExportFlowManager = get(named("backgroundExportFlowManager")),
                foregroundExportFlowManager = get(named("foregroundExportFlowManager")),
                aspectRatioProvider = get(),
                exportDir = get(named("exportDir")),
                mediaFileNameHelper = get()
            )
        }

        viewModel {
            PlaybackViewModel(
                context = androidContext(),
                videoValidator = VideoGalleryResourceValidator(
                    context = androidContext()
                ),
                videoPlayer = get()
            )
        }

        factory<ExportParamsProvider> {
            CustomExportParamsProvider(
                exportDir = get(named("exportDir")),
                mediaFileNameHelper = get(),
                watermarkBuilder = get()
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

        /**
         * Override to run export in foreground mode.
         */
        single<ExportFlowManager>(named("foregroundExportFlowManager")) {
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

        /**
         * Override to run export in background mode.
         */
        single<ExportFlowManager>(named("backgroundExportFlowManager")) {
            BackgroundExportFlowManager(
                exportDataProvider = get(),
                sessionParamsProvider = get(),
                exportSessionHelper = get(),
                exportNotificationManager = get(),
                exportDir = get(named("exportDir")),
                publishManager = get(),
                errorParser = get(),
                exportBundleProvider = get()
            )
        }
    }
}

private class CustomExportParamsProvider(
    private val exportDir: Uri,
    private val mediaFileNameHelper: MediaFileNameHelper,
    private val watermarkBuilder: WatermarkBuilder
) : ExportParamsProvider {

    override fun provideExportParams(
        effects: Effects,
        videoRangeList: VideoRangeList,
        musicEffects: List<MusicEffect>,
        videoVolume: Float
    ): List<ExportParams> {
        val exportSessionDir = exportDir.toFile().apply {
            // Export dir must be created
            mkdirs()
        }

        // Specify name for your exported video. Do not use ext i.e. .mp4
        val exportVideoFileName = mediaFileNameHelper.generateExportName() + "_watermark"

        val paramsHdWithWatermark =
            ExportParams.Builder(VideoResolution.Exact.HD) // Video Quality resolution
                .effects(effects.withWatermark(watermarkBuilder, WatermarkAlignment.BottomRight(marginRightPx = 16.toPx)))
                .fileName(exportVideoFileName)
                .debugEnabled(true)
                .videoRangeList(videoRangeList)
                .destDir(exportSessionDir)
                .musicEffects(musicEffects)
                .volumeVideo(videoVolume)
                .build()

        return listOf(paramsHdWithWatermark)
    }
}
