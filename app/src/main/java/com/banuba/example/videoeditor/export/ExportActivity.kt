package com.banuba.example.videoeditor.export

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.banuba.example.videoeditor.databinding.ActivityExportBinding
import com.banuba.sdk.core.Rotation
import com.banuba.sdk.core.domain.AspectRatioProvider
import com.banuba.sdk.core.domain.VideoSourceType
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.export.data.ExportFlowManager
import com.banuba.sdk.export.data.ExportResult
import com.banuba.sdk.export.data.ExportStopReason
import com.banuba.sdk.export.data.ExportTaskParams
import com.banuba.sdk.ve.domain.VideoRangeList
import com.banuba.sdk.ve.domain.VideoRecordRange
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File

/**
 * The sample demonstrates how to start export video using Export API in background or foreground modes.
 * - Background - when the user can interact with UI
 * - Foreground - when the user has to wait when export finishes.
 * Since export process requires initial video files we made quick integration with Gallery
 * that allows to choose video files and apply it in in export.
 */
class ExportActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ExportActivity"
    }

    private var isBackgroundExport = true

    // Step1
    // Declare ExportFlowManager for your app - foreground or background(user can interact with UI).
    private val backgroundExportFlowManager: ExportFlowManager by inject(named("backgroundExportFlowManager"))
    private val foregroundExportFlowManager: ExportFlowManager by inject(named("foregroundExportFlowManager"))
    private val aspectRatioProvider: AspectRatioProvider by inject()

    // Step 2
    // Create result observer - all export execution results are delivered here.
    private val exportResultObserver = Observer<ExportResult> { exportResult ->
        when (exportResult) {
            is ExportResult.Progress -> {
                showProgress(true)
                allowPlayingVideo(false)

                Log.d(TAG, "Export video is in progress")
                binding.previewImageView.setImageURI(exportResult.preview)
            }

            is ExportResult.Success -> {
                showProgress(false)
                allowPlayingVideo(true)
                showToast("Export video finished successfully!")

                // We take the first video for simplicity. Export can return list of video.
                val exportedVideo = exportResult.videoList.first().sourceUri
                Log.d(TAG, "Export video finished successfully = $exportedVideo")
                binding.playViewButton.setOnClickListener {
                    openVideo(exportedVideo)
                }
            }

            is ExportResult.Error -> {
                showProgress(false)
                allowPlayingVideo(false)

                showToast("Export video failed!")
                Log.e(TAG, "Error while exporting video = ${exportResult.type.messageResId}")
            }

            is ExportResult.Inactive, is ExportResult.Stopped -> {
                showProgress(false)
                allowPlayingVideo(false)
                Log.d(TAG, "Export video stopped")
            }
        }
    }

    private lateinit var binding: ActivityExportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Step 3
        // Observe export results depends on flow - foreground or background
        backgroundExportFlowManager.resultData.observe(this, exportResultObserver)
        foregroundExportFlowManager.resultData.observe(this, exportResultObserver)

        initViews()
    }

    /**
     * Step 4
     * Prepare VideoRangeList to start export.
     * It requires playFromMs and playToMs arguments to be set when creating the VideoRecordRange object.
     * Code below uses a range from 0 to video length for each video.
     */
    private fun prepareVideoRages(videosUri: List<Uri>): VideoRangeList {
        val videoRecords = videosUri.map { fileUri ->
            val videoDuration = DurationExtractor().extractDurationMilliSec(this, fileUri)
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

    // Step 5
    // Start Export flow!
    private fun startExportFlow(videosUri: List<Uri>) {
        val videoRanges = prepareVideoRages(videosUri)

        val totalVideoDuration = videoRanges.data.sumOf { it.durationMs }

        val effects = ExportEffectsProvider().provideEffects(applicationContext, totalVideoDuration)

        val coverFrameSize = Size(720, 1280)

        val exportTaskParams = ExportTaskParams(
            videoRanges = videoRanges,
            effects = effects,
            musicEffects = emptyList(),
            videoVolume = 1F,
            coverFrameSize = coverFrameSize,
            aspect = aspectRatioProvider.provide()        //by default provided aspect ratio = 9.0 / 16
        )

        if (isBackgroundExport) {
            backgroundExportFlowManager.startExport(exportTaskParams)
        } else {
            foregroundExportFlowManager.startExport(exportTaskParams)
        }
    }

    /**
     * Sample specific code.
     */
    private fun initViews() {
        binding.backgroundExportBtn.setOnClickListener {
            pickPredefinedVideos(true)
        }

        binding.foregroundExportBtn.setOnClickListener {
            pickPredefinedVideos(false)
        }

        binding.stopExportBtn.setOnClickListener {
            if (isBackgroundExport) {
                backgroundExportFlowManager.stopExport(ExportStopReason.CANCEL)
            } else {
                foregroundExportFlowManager.stopExport(ExportStopReason.CANCEL)
            }
        }
    }

    private fun pickPredefinedVideos(isBackground: Boolean) {
        binding.previewImageView.setImageURI(null)
        isBackgroundExport = isBackground

        pickSampleVideos.launch("video/*")
    }

    private val pickSampleVideos = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { videosUri ->
        if (videosUri.isNullOrEmpty()) return@registerForActivityResult

        showProgress(true)

        startExportFlow(videosUri)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun showProgress(isExporting: Boolean) {
        if (!isBackgroundExport) {
            binding.exportProgressView.isVisible = isExporting
        }
        binding.backgroundExportBtn.isEnabled = !isExporting
        binding.foregroundExportBtn.isEnabled = !isExporting
    }

    private fun allowPlayingVideo(flag: Boolean) {
        binding.playViewButton.visibility = if (flag) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun openVideo(videoUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${packageName}.provider",
                File(videoUri.encodedPath)
            )
            setDataAndType(uri, "video/mp4")
        }
        startActivity(intent)
    }
}