package com.banuba.example.videoeditor.export

import android.net.Uri
import androidx.core.net.toFile
import com.banuba.sdk.core.VideoResolution
import com.banuba.sdk.core.ext.toPx
import com.banuba.sdk.core.media.MediaFileNameHelper
import com.banuba.sdk.export.data.ExportParams
import com.banuba.sdk.export.data.ExportParamsProvider
import com.banuba.sdk.ve.domain.VideoRangeList
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.music.MusicEffect
import com.banuba.sdk.ve.effects.watermark.WatermarkAlignment
import com.banuba.sdk.ve.effects.watermark.WatermarkBuilder
import com.banuba.sdk.ve.ext.withWatermark

class CustomExportParamsProvider(
    private val exportDir: Uri,
    private val mediaFileNameHelper: MediaFileNameHelper,
    private val watermarkBuilder: WatermarkBuilder
) : ExportParamsProvider {

    private val addSoundtrackFile = false

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

        val extraSoundtrackUri = if (addSoundtrackFile) {
            Uri.parse(exportSessionDir.toString()).buildUpon()
                .appendPath(mediaFileNameHelper.generateExportSoundtrackFileName())
                .build()
        } else {
            Uri.EMPTY
        }

        // Specify name for your exported video. Do not use ext i.e. .mp4
        val exportVideoFileName = "exported_video"

        val paramsHdWithWatermark =
            ExportParams.Builder(VideoResolution.Exact.HD) // Video Quality resolution
                .effects(effects.withWatermark(watermarkBuilder, WatermarkAlignment.BottomRight(marginRightPx = 16.toPx)))
                .fileName(exportVideoFileName)
                .debugEnabled(true)
                .videoRangeList(videoRangeList)
                .destDir(exportSessionDir)
                .musicEffects(musicEffects)
                .volumeVideo(videoVolume)
                .extraAudioFile(extraSoundtrackUri)
                .build()

        return listOf(paramsHdWithWatermark)
    }
}
