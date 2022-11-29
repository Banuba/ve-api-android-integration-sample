package com.banuba.example.videoeditor.export

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.banuba.example.videoeditor.databinding.ActivityExportBinding
import com.banuba.sdk.export.data.ExportResult
import org.koin.androidx.viewmodel.ext.android.viewModel
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
        const val TAG = "ExportSample"
    }

    private val viewModel by viewModel<ExportViewModel>()

    // Create result observer - all export execution results are delivered here.
    private val exportResultObserver = Observer<ExportResult> { exportResult ->
        when (exportResult) {
            is ExportResult.Progress -> {
                showProgress(true)
                allowPlayingVideo(false)
                allowStop(true)

                Log.d(TAG, "Export video is in progress")
                binding.previewImageView.setImageURI(exportResult.preview)
            }

            is ExportResult.Success -> {
                showProgress(false)
                allowPlayingVideo(true)
                allowStop(false)
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
                allowStop(false)

                showToast("Export video failed!")
                Log.e(TAG, "Error while exporting video = ${exportResult.type.messageResId}")
            }

            is ExportResult.Inactive, is ExportResult.Stopped -> {
                showProgress(false)
                allowPlayingVideo(false)
                allowStop(false)
                Log.d(TAG, "Export video stopped")
            }
        }
    }

    private lateinit var binding: ActivityExportBinding

    private val pickSampleVideos = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { videosUri ->
        if (videosUri.isNullOrEmpty()) {
            showToast("Please select video to proceed export!")
            Log.w(TAG, "No video selected to proceed export!")
            return@registerForActivityResult
        }

        showProgress(true)

        viewModel.startExportVideoSample(videosUri, isForegroundMode())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe export results depends on flow - foreground or background
        viewModel.exportBackgroundData.observe(this, exportResultObserver)
        viewModel.exportForegroundData.observe(this, exportResultObserver)

        initViews()
    }

    /**
     * Sample specific code.
     */
    private fun initViews() {
        binding.startExportButton.setOnClickListener {
            binding.previewImageView.setImageURI(null)
            pickSampleVideos.launch("video/*")
        }

        binding.stopExportBtn.setOnClickListener {
            viewModel.stopExport()
        }

        binding.startSlideshowExportBtn.setOnClickListener {
            viewModel.startSlideshowVideoSample(true)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun showProgress(isExporting: Boolean) {
        binding.exportProgressView.isVisible = isExporting
        binding.startExportButton.isEnabled = !isExporting
        binding.foregroundModeSwitch.isEnabled = !isExporting
    }

    private fun allowPlayingVideo(flag: Boolean) {
        binding.playViewButton.visibility = if (flag) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun allowStop(flag: Boolean) {
        binding.stopExportBtn.visibility = if (flag) {
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

    private fun isForegroundMode(): Boolean = binding.foregroundModeSwitch.isChecked
}