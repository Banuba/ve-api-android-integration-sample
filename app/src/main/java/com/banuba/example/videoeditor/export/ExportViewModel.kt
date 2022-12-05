package com.banuba.example.videoeditor.export

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.ParcelUuid
import android.util.Log
import android.util.Size
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.banuba.example.videoeditor.editor.EditorViewModel
import com.banuba.sdk.core.CoroutineDispatcherProvider
import com.banuba.sdk.core.Rotation
import com.banuba.sdk.core.domain.AspectRatioProvider
import com.banuba.sdk.core.domain.VideoSourceType
import com.banuba.sdk.core.ext.copyFromAssetsToExternal
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.core.media.MediaFileNameHelper
import com.banuba.sdk.export.data.ExportFlowManager
import com.banuba.sdk.export.data.ExportStopReason
import com.banuba.sdk.export.data.ExportTaskParams
import com.banuba.sdk.ve.domain.VideoRangeList
import com.banuba.sdk.ve.domain.VideoRecordRange
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.VisualTimedEffect
import com.banuba.sdk.ve.slideshow.SlideShowSource
import com.banuba.sdk.ve.slideshow.SlideShowTask
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class ExportViewModel(
    appContext: Application,
    private val backgroundExportFlowManager: ExportFlowManager,
    private val foregroundExportFlowManager: ExportFlowManager,
    private val aspectRatioProvider: AspectRatioProvider,
    private val exportDir: Uri,
    private val mediaFileNameHelper: MediaFileNameHelper,
) : AndroidViewModel(appContext) {

    companion object {
        private const val SLIDESHOW_ITEM_DURATION_MS = 3000L // 1 picture takes 3 seconds video
    }

    private var runInForeground = true

    val exportBackgroundData = backgroundExportFlowManager.resultData
    val exportForegroundData = foregroundExportFlowManager.resultData

    private var currentExportFlowManager: ExportFlowManager? = null

    private val exportEffectsProvider = ExportEffectsProvider()

    fun stopExport() {
        Log.d(ExportActivity.TAG, "Stop export = $currentExportFlowManager")
        currentExportFlowManager?.stopExport(ExportStopReason.CANCEL)
        currentExportFlowManager = null
    }

    fun startExportVideoSample(videosUri: List<Uri>, inForeground: Boolean) {
        val context = getApplication<Application>().applicationContext

        val videoRanges = prepareVideoRages(videosUri)
        val totalVideoDuration = videoRanges.data.sumOf { it.durationMs }
        val effects = exportEffectsProvider.provideEffects(context, totalVideoDuration)
        val coverFrameSize = Size(720, 1280)

        val params = ExportTaskParams(
            videoRanges = videoRanges,
            effects = effects,
            musicEffects = emptyList(),
            videoVolume = 1F,
            coverFrameSize = coverFrameSize,
            aspect = aspectRatioProvider.provide()        //by default provided aspect ratio = 9.0 / 16
        )

        startExportInternal(params, inForeground)
    }


    fun startSlideshowVideoSample(inForeground: Boolean) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext

            // Specify slideshow video resolution you want to have in your exported video
            val videoFHD = Size(1080, 1920)

            // Specify name for slideshow video
            val slideShowFileName = mediaFileNameHelper.generateExportName()


            // Specify uri where video will be stored
            val destSlideshowUri = exportDir
                .toFile()
                .apply {
                    // Export dir must be created
                    mkdirs()
                }.toUri()
                .buildUpon()
                .appendPath(slideShowFileName)
                .build()

            // Specify picture uri
            val sourcePictureUri = context.copyFromAssetsToExternal("picture_slideshow.png").toUri()

            try {
                Log.d(ExportActivity.TAG, "Start slideshow = $destSlideshowUri")
                createSlideshowVideo(
                    context,
                    File(destSlideshowUri.path),
                    videoFHD,
                    listOf(sourcePictureUri)
                )
                Log.d(ExportActivity.TAG, "Slideshow created = $destSlideshowUri")
            } catch (e: Throwable) {
                Log.w(ExportActivity.TAG, "Failed to create slideshow = $destSlideshowUri", e)
                // Failed to create slideshow video. Cannot continue. Exit!
                return@launch
            }

            // Specify effects
            val visualStack = Stack<VisualTimedEffect>().apply {
                add(exportEffectsProvider.createTextVisualEffect())
            }

            val effects = Effects(
                speedStack = Stack(),
                visualStack = visualStack
            )

            // Specify list of video to export
            val videosUri = listOf(destSlideshowUri)
            val videoRanges = prepareVideoRages(videosUri)

            // Specify audio in video
            val audioUri = context.copyFromAssetsToExternal("sample_audio.mp3").toUri()

            val musicEffect = EditorViewModel.PlaybackMusicEffect(
                uuid = ParcelUuid(UUID.randomUUID()),
                sourceUri = audioUri,
                playUri = audioUri,
                equalizerEffect = null,
                startOnSourceMs = 0L,
                startOnTimelineMs = 0L,
                effectDurationMs = 0L,
                normalSpeedEffectDurationMs = SLIDESHOW_ITEM_DURATION_MS,
                volume = 1F
            )

            val params = ExportTaskParams(
                videoRanges = videoRanges,
                effects = effects,
                musicEffects = listOf(musicEffect),
                videoVolume = 1F,
                coverFrameSize = videoFHD,
                aspect = aspectRatioProvider.provide()   //by default provided aspect ratio = 9.0 / 16
            )

            startExportInternal(params, inForeground)
        }
    }

    private suspend fun createSlideshowVideo(
        context: Context,
        destExportFile: File,
        videoResolution: Size,
        sourcePictureUriList: List<Uri>
    ) = withContext(CoroutineDispatcherProvider.IO) {
        val sources = sourcePictureUriList.map { uri ->
            SlideShowSource.File(
                durationMs = SLIDESHOW_ITEM_DURATION_MS,
                source = uri
            )
        }

        val params = SlideShowTask.Params.create(
            context = context,
            size = videoResolution,
            destFile = destExportFile,
            sources = sources
        )
        SlideShowTask.makeVideo(params)
    }

    private fun startExportInternal(
        params: ExportTaskParams,
        inForeground: Boolean
    ) {
        runInForeground = inForeground

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
                type = VideoSourceType.GALLERY,  //mandatory, type of video source (gallery, camera, slideshow)
                pipApplied = false
            )
        }
        return VideoRangeList(videoRecords)
    }
}