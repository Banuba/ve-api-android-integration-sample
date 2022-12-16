package com.banuba.example.videoeditor.playback

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.banuba.example.videoeditor.R
import com.banuba.example.videoeditor.databinding.ActivityExportBinding
import com.banuba.example.videoeditor.databinding.ActivityPlaybackBinding
import com.banuba.example.videoeditor.utils.GetMultipleContents
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaybackActivity : AppCompatActivity() {

    private val viewModel by viewModel<PlaybackViewModel>()

    private lateinit var binding: ActivityPlaybackBinding

    private val selectVideos = registerForActivityResult(GetMultipleContents()) {
        val predefinedVideos = it.toTypedArray()
        if (predefinedVideos.isNotEmpty()) {
            viewModel.addVideosToPlayback(predefinedVideos)
        }
    }

    private val selectMusicTrack = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            binding.musicEffectCheckBox.isChecked = false
            return@registerForActivityResult
        }
        viewModel.addMusicToPlayback(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.videoPlayerSelectVideos.setOnClickListener {
            selectVideos.launch("video/*")
        }

        binding.videoPlayerMakeScreenshot.setOnClickListener {
            viewModel.takeScreenshot()
        }

        binding.videoPlaybackBtn.setOnClickListener {
            if (binding.videoPlaybackBtn.isActivated) {
                viewModel.pause()
            } else {
                viewModel.play()
            }
            binding.videoPlaybackBtn.isActivated = !binding.videoPlaybackBtn.isActivated
        }

        binding.videoRewindBtn.setOnClickListener {
            viewModel.rewind()
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

        binding.musicEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectMusicTrack.launch("audio/*")
            } else {
                viewModel.removeMusicEffect()
            }
        }

        binding.customEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.applyCustomEffect()
            } else {
                viewModel.remoteCustomEffect()
            }
        }

        binding.videoPlayerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.videoPlayerVolumeSeekBar.max = 100
        binding.videoPlayerVolumeSeekBar.progress = 100
        binding.videoPlayerVolumeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setVolume(progress.toFloat() / 100)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.screenshotCardView.setOnClickListener {
            binding.screenshotCardView.visibility = View.GONE
        }

        viewModel.errorMsg.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        viewModel.totalDuration.observe(this) { totalDuration ->
            binding.videoPlayerSeekBar.max = totalDuration
        }

        viewModel.playbackPosition.observe(this) { position ->
            binding.videoPlayerSeekBar.progress = position
        }

        viewModel.screenshotBitmap.observe(this) { screenshot ->
            binding.screenshotImageView.setImageBitmap(screenshot)
            binding.screenshotCardView.visibility = View.VISIBLE
        }

        viewModel.prepare(binding.videoPlayerSurfaceView.holder)

    }

    override fun onStop() {
        binding.videoPlaybackBtn.isActivated = false
        viewModel.pause()
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.releasePlayer(binding.videoPlayerSurfaceView.holder)
        super.onDestroy()
    }


}