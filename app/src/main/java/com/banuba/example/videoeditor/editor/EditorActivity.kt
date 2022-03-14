package com.banuba.example.videoeditor.editor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.banuba.example.videoeditor.databinding.ActivityEditorBinding
import com.banuba.sdk.ve.data.ExportResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditorActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_PREDEFINED_VIDEOS = "EXTRA_PREDEFINED_VIDEOS"

        fun createIntent(context: Context, predefinedVideos: List<Uri>): Intent {
            return Intent(context, EditorActivity::class.java).apply {
                val listData = ArrayList(predefinedVideos)
                putParcelableArrayListExtra(EXTRA_PREDEFINED_VIDEOS, listData)
            }
        }
    }

    private lateinit var binding: ActivityEditorBinding

    private val viewModel by viewModel<EditorViewModel>()

    private var isMusicSet = false

    private val selectMusicTrack =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            isMusicSet = uri?.let {
                viewModel.addMusicToPlayback(uri)
                true
            } ?: false
            binding.musicEffectCheckBox.isActivated = isMusicSet
        }

    private val predefinedVideos by lazy(LazyThreadSafetyMode.NONE) {
        intent.getParcelableArrayListExtra<Uri>(EXTRA_PREDEFINED_VIDEOS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        predefinedVideos?.toTypedArray()?.let {
            viewModel.addVideosToPlayback(it)
        }

        viewModel.errorMsg.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        viewModel.exportResultData.observe(this) { exportResult ->
            when (exportResult) {
                is ExportResult.Inactive, is ExportResult.Stopped -> showProgress(false)
                is ExportResult.Progress -> showProgress(true)
                is ExportResult.Success -> {
                    showProgress(false)
                    Toast.makeText(
                        this,
                        "Export Success: ${exportResult.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ExportResult.Error -> {
                    showProgress(false)
                    Toast.makeText(
                        this,
                        getString(exportResult.type.messageResId),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.fxEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.applyFxEffect()
            } else {
                viewModel.removeFxEffect()
            }
        }

        binding.textEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.applyTextEffect()
            } else {
                viewModel.removeTextEffect()
            }
        }

        binding.gifEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.applyGifEffect()
            } else {
                viewModel.removeGifEffect()
            }
        }

        binding.speedEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.applySpeedEffect()
            } else {
                viewModel.removeSpeedEffect()
            }
        }

        binding.lutEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.applyLutEffect()
            } else {
                viewModel.removeLutEffect()
            }
        }

        binding.musicEffectCheckBox.setOnClickListener {
            if (isMusicSet) {
                it.isActivated = false
                viewModel.removeMusicEffect()
            } else {
                selectMusicTrack.launch("audio/*")
            }
        }

        binding.exportButton.setOnClickListener {
            showProgress(true)
            viewModel.startExport()
        }

        viewModel.prepare(binding.videoPlayerSurfaceView.holder)
    }

    override fun onStart() {
        super.onStart()
        viewModel.play()
    }

    override fun onStop() {
        viewModel.pause()
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.releasePlayer(binding.videoPlayerSurfaceView.holder)
        super.onDestroy()
    }

    private fun showProgress(show: Boolean) {
        binding.exportProgressBar.isVisible = show
    }
}
