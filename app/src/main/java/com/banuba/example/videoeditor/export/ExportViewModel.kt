package com.banuba.example.videoeditor.export

import android.app.Application
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import com.banuba.sdk.core.Rotation
import com.banuba.sdk.core.domain.AspectRatioProvider
import com.banuba.sdk.core.domain.VideoSourceType
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.export.data.ExportFlowManager
import com.banuba.sdk.export.data.ExportStopReason
import com.banuba.sdk.export.data.ExportTaskParams
import com.banuba.sdk.ve.domain.VideoRangeList
import com.banuba.sdk.ve.domain.VideoRecordRange

class ExportViewModel(
    appContext: Application,
    private val backgroundExportFlowManager: ExportFlowManager,
    private val foregroundExportFlowManager: ExportFlowManager,
    private val aspectRatioProvider: AspectRatioProvider
) : AndroidViewModel(appContext) {
    private var runInForeground = true

    val exportBackgroundData = backgroundExportFlowManager.resultData
    val exportForegroundData = foregroundExportFlowManager.resultData

    private var currentExportFlowManager: ExportFlowManager? = null

    fun stopExport() {
        Log.d(ExportActivity.TAG, "Stop export = $currentExportFlowManager")
        currentExportFlowManager?.stopExport(ExportStopReason.CANCEL)
        currentExportFlowManager = null
    }

    // Start Export flow!
    fun startExport(videosUri: List<Uri>, inForeground: Boolean) {
        runInForeground = inForeground
        val context = getApplication<Application>().applicationContext

        val videoRanges = prepareVideoRages(videosUri)
        val totalVideoDuration = videoRanges.data.sumOf { it.durationMs }
        val effects = ExportEffectsProvider().provideEffects(context, totalVideoDuration)
        val coverFrameSize = Size(720, 1280)

        val params = ExportTaskParams(
            videoRanges = videoRanges,
            effects = effects,
            musicEffects = emptyList(),
            videoVolume = 1F,
            coverFrameSize = coverFrameSize,
            aspect = aspectRatioProvider.provide()        //by default provided aspect ratio = 9.0 / 16
        )

        currentExportFlowManager = if (inForeground) {
            foregroundExportFlowManager
        } else {
            backgroundExportFlowManager
        }

        Log.d(ExportActivity.TAG, "Start export task = $params, flow = $currentExportFlowManager")
        requireNotNull(currentExportFlowManager).startExport(params)
    }

    /**
     * Prepare VideoRangeList to start export.
     * It requires playFromMs and playToMs arguments to be set when creating the VideoRecordRange object.
     * Code below uses a range from 0 to video length for each video.
     */
    private fun prepareVideoRages(videosUri: List<Uri>): VideoRangeList {
        val context = getApplication<Application>().applicationContext

        val videoRecords = videosUri.map { fileUri ->
            val videoDuration = DurationExtractor().extractDurationMilliSec(context, fileUri)
            val videoSpeed = 1f
            VideoRecordRange(
                sourceUri = fileUri,            //mandatory, uri of video file
                durationMs = videoDuration,     //mandatory, duration of video
                speed = videoSpeed,             //mandatory, video playback speed
                playFromMs = 0,                 //optional, by default equals 0
                playToMs = videoDuration,       //optional, by default equals duration of video,
                rotation = Rotation.ROTATION_0,  //optional, by default ROTATION_0
                type = VideoSourceType.GALLERY  //mandatory, type of video source (gallery, camera, slideshow)
            )
        }
        return VideoRangeList(videoRecords)
    }
}