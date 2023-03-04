package com.banuba.example.videoeditor.playback

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.banuba.example.videoeditor.databinding.ActivityPlaybackBinding
import com.banuba.example.videoeditor.utils.GetMultipleContents
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaybackActivity : AppCompatActivity() {

    companion object {
        private const val MASK_EFFECT_NAME = "AsaiLines"
    }

    private val viewModel by viewModel<PlaybackViewModel>()

    private lateinit var binding: ActivityPlaybackBinding

    private val selectVideos = registerForActivityResult(GetMultipleContents()) {
        val predefinedVideos = it.toTypedArray()
        val hasVideoContent = predefinedVideos.isNotEmpty()

        if (hasVideoContent) {
            binding.playbackContainer.visibility = View.VISIBLE
            binding.pickVideoButton.visibility = View.GONE

            viewModel.addVideoContent(predefinedVideos)
        } else {
            showToast("Please pick video content to proceed")

            binding.playbackContainer.visibility = View.GONE
            binding.pickVideoButton.visibility = View.VISIBLE
        }
    }

    private val selectMusicTrack = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            binding.musicEffectCheckBox.isChecked = false
            return@registerForActivityResult
        }
        viewModel.addMusicTrack(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pickVideoButton.setOnClickListener {
            selectVideos.launch("video/*")
        }

        binding.takeScreenShotButton.setOnClickListener {
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
                showToast("Applied Visual FX effect")
                viewModel.addFxEffect()
            } else {
                viewModel.removeFxEffect()
            }
        }

        binding.maskEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied AR effect")
                viewModel.addMaskEffect(MASK_EFFECT_NAME)
            } else {
                viewModel.removeMaskEffect()
            }
        }

        binding.textEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied Text effect")
                viewModel.addTextEffect()
            } else {
                viewModel.removeTextEffect()
            }
        }

        binding.gifEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied Sticker effect")
                viewModel.addStickerEffect()
            } else {
                viewModel.removeGifEffect()
            }
        }

        binding.rapidEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied Rapid Speed effect")
                viewModel.addRapidSpeedEffect()
            } else {
                viewModel.removeSpeedEffect()
            }
        }

        binding.slowMotionEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied Slow Motion Speed effect")
                viewModel.addSlowMotionSpeedEffect()
            } else {
                viewModel.removeSpeedEffect()
            }
        }

        binding.lutEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied Color effect")
                viewModel.addColorEffect()
            } else {
                viewModel.removeColorEffect()
            }
        }

        binding.musicEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectMusicTrack.launch("audio/*")
            } else {
                viewModel.removeMusicTrack()
            }
        }

        binding.blurEffectCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showToast("Applied Blur effect")
                viewModel.addBlurEffect()
            } else {
                viewModel.remoteBlurEffect()
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
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                viewModel.setVolume(progress.toFloat() / 100)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.screenshotCardView.setOnClickListener {
            binding.screenshotCardView.visibility = View.GONE
        }

        binding.seekForwardButton.setOnClickListener { viewModel.seekForward() }
        binding.seekBackwardButton.setOnClickListener { viewModel.seekBackward() }

        viewModel.errorMessageData.observe(this) { message ->
            showToast(message)
        }

        viewModel.totalDurationData.observe(this) { totalDuration ->
            binding.videoPlayerSeekBar.max = totalDuration
        }

        viewModel.playbackPositionData.observe(this) { position ->
            binding.videoPlayerSeekBar.progress = position
        }

        viewModel.screenshotBitmapData.observe(this) { screenshot ->
            binding.screenshotImageView.setImageBitmap(screenshot)
            binding.screenshotCardView.visibility = View.VISIBLE
        }

        viewModel.preparePlayer(binding.videoPlayerSurfaceView.holder)
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

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}