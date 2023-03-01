package com.banuba.example.videoeditor.playback

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelUuid
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banuba.example.videoeditor.SampleEffectsProvider
import com.banuba.example.videoeditor.utils.BanubaEffectHelper
import com.banuba.sdk.core.data.MediaDataGalleryValidator
import com.banuba.sdk.core.data.MediaValidationResultType
import com.banuba.sdk.core.effects.DrawType
import com.banuba.sdk.core.effects.FadeEffect
import com.banuba.sdk.core.gl.GlViewport
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.effects.ve.VideoEffectsHelper
import com.banuba.sdk.playback.PlaybackError
import com.banuba.sdk.playback.PlayerScaleType
import com.banuba.sdk.playback.VideoPlayer
import com.banuba.sdk.ve.effects.TypedTimedEffect
import com.banuba.sdk.ve.effects.VisualTimedEffect
import com.banuba.sdk.ve.ext.DurationHelper
import com.banuba.sdk.ve.ext.VideoEditorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

const val TAG = "PlaybackSample"

class PlaybackViewModel(
    private val context: Context,
    private val videoValidator: MediaDataGalleryValidator,
    private val videoPlayer: VideoPlayer
) : ViewModel() {
    // Defines currently playing position
    val playbackPositionData = MutableLiveData<Int>()

    // Defines total video duration
    val totalDurationData = MutableLiveData<Int>()

    // Defines taken screenshot from video player
    val screenshotBitmapData = MutableLiveData<Bitmap>()

    // Defines an error message that can occur in video player
    val errorMessageData = MutableLiveData<String>()

    private var viewportSize = Size(720, 1280)

    /**
     * Defines effects applied to video in playback - not stored in video.
     * Use Export for storing effects in video.
     */
    private val appliedEffects = mutableListOf<TypedTimedEffect<*>>()

    private val videoPlayerCallback = object : VideoPlayer.Callback {
        override fun onScreenshotTaken(bmp: Bitmap) {
            screenshotBitmapData.postValue(bmp)
        }

        override fun onVideoPlaybackError(error: PlaybackError) {
            errorMessageData.postValue(error.toString())
        }

        override fun onVideoPositionChanged(positionMs: Int) {
            playbackPositionData.postValue(positionMs)
        }

        override fun onViewportChanged(viewport: GlViewport) {
            Log.d(
                TAG,
                "Video view port changed : (${viewport.x}, ${viewport.y}), Size(${viewport.width}x${viewport.height})"
            )
            viewportSize = Size(viewport.width, viewport.height)
        }
    }

    private val durationHelper = DurationHelper()

    /**
     * Adds video content to video player.
     * Every video file is validated using Banuba validator.
     * Next, every validated URI is converted to VideoRecordRange.
     */
    fun addVideoContent(videoContent: Array<Uri>) {
        // We highly recommend not to process in the Main thread
        viewModelScope.launch(Dispatchers.IO) {
            val videoPlaylist = videoContent
                .filter { videoValidator.getValidationResult(it) == MediaValidationResultType.VALID_FILE }
                .mapNotNull {
                    VideoEditorUtils.createVideoRecordRange(
                        sourceUri = it,
                        context = context,
                        pipApplied = false
                    )
                }

            // Calculate total video duration in milliseconds
            val totalVideoDurationMs = durationHelper.run {
                setVideoRanges(videoPlaylist)
                this.totalDuration
            }

            totalDurationData.postValue(totalVideoDurationMs)

            // Set video content to video player
            videoPlayer.setVideoRanges(videoPlaylist)
        }
    }

    /**
     * Prepares video player for playing video content.
     */
    fun preparePlayer(surfaceHolder: SurfaceHolder) {
        if (videoPlayer.prepare(Size(1920, 1080))) {
            with(videoPlayer) {
                setSurfaceHolder(surfaceHolder)
                setCallback(videoPlayerCallback)
                setScaleType(PlayerScaleType.CENTER_INSIDE)
                setVideoSize(Size(1024, 768))
                setVolume(1f)
            }
        } else {
            errorMessageData.value = "Error while preparing video player"
        }
    }

    fun play() {
        videoPlayer.play(true)
    }

    fun pause() {
        videoPlayer.pause()
    }

    fun rewind() {
        videoPlayer.seekTo(0)
    }

    fun seekTo(position: Int) {
        videoPlayer.seekTo(position)
    }

    fun setVolume(value: Float) {
        videoPlayer.setVolume(value)
    }

    fun releasePlayer(surfaceHolder: SurfaceHolder) {
        videoPlayer.clearSurfaceHolder(surfaceHolder)
        videoPlayer.release()
    }

    fun takeScreenshot() {
        videoPlayer.takeScreenshot()
    }

    fun addMusicTrack(uri: Uri) {
        // We highly recommend not to process in the Main thread
        viewModelScope.launch(Dispatchers.IO) {
            val trackDurationMs =
                DurationExtractor(video = false).extractDurationMilliSec(context, uri)

            val musicEffect = SampleEffectsProvider.MusicTrackEffect(
                uuid = ParcelUuid(UUID.randomUUID()),
                sourceUri = uri,
                startOnTimelineMs = 0,
                startOnSourceMs = 0,
                effectDurationMs = trackDurationMs,
                normalSpeedEffectDurationMs = trackDurationMs,
                volume = 1f,
                playUri = uri,
                equalizerEffect = null,
                fadeEffect = FadeEffect.EMPTY
            )
            // Video Player supports playing multiple music track.
            // In this sample single music track is added for simplicity
            val musicTracks = listOf(musicEffect)

            videoPlayer.setMusicEffects(musicTracks)
        }
    }

    fun removeMusicTrack() {
        videoPlayer.setMusicEffects(emptyList())
    }

    fun addFxEffect() {
        val fxEffect = SampleEffectsProvider.createFxEffect(context, "VHS")
        addEffectInternal(fxEffect)
    }

    fun removeFxEffect() {
        // Removes all FX effects
        removeEffectsByTypeInternal(DrawType.VISUAL)
    }

    fun addMaskEffect(effectName: String) {
        // Any AR effect (mask) should be prepared before applying.
        // Prepare means - download if needed and copied into local device storage.
        val preparedMaskEffect = BanubaEffectHelper(context).prepareEffect(effectName)

        val maskEffect = VisualTimedEffect(
            effectDrawable = VideoEffectsHelper.createMaskEffect(preparedMaskEffect.uri)
        )
        addEffectInternal(maskEffect)
    }

    fun removeMaskEffect() {
        removeEffectsByTypeInternal(DrawType.MASK)
    }

    fun addTextEffect() {
        val textEffect = SampleEffectsProvider.createTextVisualEffect("Text in Playback")
        addEffectInternal(textEffect)
    }

    fun removeTextEffect() {
        removeEffectsByTypeInternal(DrawType.TEXT)
    }

    fun addStickerEffect() {
        val stickerEffect = SampleEffectsProvider.createStickerEffect(context)
        addEffectInternal(stickerEffect)
    }

    fun removeGifEffect() {
        removeEffectsByTypeInternal(DrawType.GIF)
    }

    fun addRapidSpeedEffect() {
        val durationMs = totalDurationData.value ?: 0
        val rapidEffect = SampleEffectsProvider.createRapidSpeedEffect(durationMs.toLong())
        addEffectInternal(rapidEffect)
    }

    fun addSlowMotionSpeedEffect() {
        val durationMs = totalDurationData.value ?: 0
        val rapidEffect = SampleEffectsProvider.createSlowMotionSpeedEffect(durationMs.toLong())
        addEffectInternal(rapidEffect)
    }

    fun removeSpeedEffect() {
        removeEffectsByTypeInternal(DrawType.TIME)
    }

    fun addColorEffect() {
        val colorEffect = SampleEffectsProvider.createColorFilterEffect(context)
        addEffectInternal(colorEffect)
    }

    fun removeColorEffect() {
        removeEffectsByTypeInternal(DrawType.COLOR)
    }

    fun addCustomEffect() {
        val customEffect = SampleEffectsProvider.createCustomEffect(viewportSize)
        addEffectInternal(customEffect)
    }

    fun remoteCustomEffect() {
        removeEffectsByTypeInternal(DrawType.CUSTOM)
    }

    private fun addEffectInternal(effect: TypedTimedEffect<*>) {
        appliedEffects.add(effect)
        videoPlayer.setEffects(appliedEffects)
    }

    private fun removeEffectsByTypeInternal(@DrawType type: Int) {
        appliedEffects.removeAll { it.drawable.type == type }
        videoPlayer.setEffects(appliedEffects)
    }
}