package com.banuba.example.videoeditor.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.ParcelUuid
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banuba.example.videoeditor.export.CustomEffectDrawable
import com.banuba.sdk.core.data.MediaDataGalleryValidator
import com.banuba.sdk.core.data.MediaValidationResultType
import com.banuba.sdk.core.effects.DrawType
import com.banuba.sdk.core.effects.EqualizerEffect
import com.banuba.sdk.core.effects.FadeEffect
import com.banuba.sdk.core.effects.RectParams
import com.banuba.sdk.core.ext.copyFromAssetsToExternal
import com.banuba.sdk.core.gl.GlViewport
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.effects.ve.VideoEffectsHelper
import com.banuba.sdk.playback.PlaybackError
import com.banuba.sdk.playback.PlayerScaleType
import com.banuba.sdk.playback.VideoPlayer
import com.banuba.sdk.ve.effects.SpeedTimedEffect
import com.banuba.sdk.ve.effects.TypedTimedEffect
import com.banuba.sdk.ve.effects.VisualTimedEffect
import com.banuba.sdk.ve.effects.music.MusicEffect
import com.banuba.sdk.ve.ext.DurationHelper
import com.banuba.sdk.ve.ext.VideoEditorUtils
import com.banuba.sdk.ve.ext.setCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PlaybackViewModel(
    private val context: Context,
    private val videoValidator: MediaDataGalleryValidator,
    private val videoPlayer: VideoPlayer
) : ViewModel() {

    private val _playbackPosition = MutableLiveData<Int>()
    val playbackPosition: LiveData<Int>
        get() = _playbackPosition

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String>
        get() = _errorMsg

    private val _totalDuration = MutableLiveData<Int>()
    val totalDuration: LiveData<Int>
        get() = _totalDuration

    private val _screenshotBitmap = MutableLiveData<Bitmap>()
    val screenshotBitmap: LiveData<Bitmap>
        get() = _screenshotBitmap

    private var viewportSize = Size(720, 1280)

    /**
     * This list is used to store applied visual and speed effects
     */
    private val appliedEffects = mutableListOf<TypedTimedEffect<*>>()

    private val videoPlayerCallback = object : VideoPlayer.Callback {
        override fun onScreenshotTaken(bmp: Bitmap) {
            _screenshotBitmap.postValue(bmp)
        }

        override fun onVideoPlaybackError(error: PlaybackError) {
            _errorMsg.postValue(error.toString())
        }

        override fun onVideoPositionChanged(positionMs: Int) {
            _playbackPosition.postValue(positionMs)
        }

        override fun onViewportChanged(viewport: GlViewport) {
            Log.d(
                "BanubaVideoPlayer",
                "Video view port: (${viewport.x}, ${viewport.y}), Size(${viewport.width}x${viewport.height})"
            )
            viewportSize = Size(viewport.width, viewport.height)
        }

    }

    /**
     * While preparing selected Uris to play with BanubaVideoPlayer they are validated by MediaDataGalleryValidator
     * provided by ve-sdk module.
     * Each valid uri is converted to VideoRecordRange object required for playback by using
     * convenient extension PlaybackUtils.createVideoRecordRange.
     * To calculate the total duration here DurationHelper class in used.
     * If you need to track any changes in playlist,
     * for example, when you removing one of the VideoRecordRange objects from the list, you should pass remaining
     * VideoRecordRanges into DurationHelper and take totalDuration again.
     */
    fun addVideosToPlayback(
        videos: Array<Uri>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val playlist = videos
                .filter { videoValidator.getValidationResult(it) == MediaValidationResultType.VALID_FILE }
                .mapNotNull { VideoEditorUtils.createVideoRecordRange(sourceUri = it,
                    context =  context,
                    pipApplied = false
                ) }
            _totalDuration.postValue(DurationHelper().run {
                setVideoRanges(playlist)
                this.totalDuration
            })
            videoPlayer.setVideoRanges(playlist)
        }
    }

    /**
     * BanubaVideoPlayer consumes MusicEffects list to play music over the video.
     * Here custom PlaybackMusicEffect data class is used, but you can create your own implementation.
     *
     * startOnTimelineMs sets the position on the video playback (0 means the starting position)
     * startOnSourceMs sets the position on the sourceUri that is used to play selected music track.
     *
     * To calculate music track duration here is used DurationExtractor helper class from core-sdk module.
     *
     * Created musicEffect is applied from the start of video playlist and start to play from the very beginning.
     */
    fun addMusicToPlayback(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val effectDuration =
                DurationExtractor(video = false).extractDurationMilliSec(context, uri)
            val musicEffect = PlaybackMusicEffect(
                uuid = ParcelUuid(UUID.randomUUID()),
                sourceUri = uri,
                startOnTimelineMs = 0,
                startOnSourceMs = 0,
                effectDurationMs = effectDuration,
                normalSpeedEffectDurationMs = effectDuration,
                volume = 1f,
                playUri = uri,
                equalizerEffect = null,
                fadeEffect = FadeEffect.EMPTY
            )
            videoPlayer.setMusicEffects(listOf(musicEffect))
        }
    }

    /**
     * Before using BanubaVideoPlayer it should be prepared.
     * To track video position, errors and taken screenshots videoPlayerCallback passed into player.
     * PlayerScaleType defines how the video should be scaled into SurfaceView:
     * here CENTER_INSIDE and FIT_SCREEN_HEIGHT values are available.
     * You can define video size but keep in mind that
     * the real size of the drawing field is calculated under the hood and depends
     * on the size of SurfaceView.
     * Also here we set up video volume (it is measured from 0 to 1).
     */
    fun prepare(surfaceHolder: SurfaceHolder) {
        if (videoPlayer.prepare(Size(1920, 1080))) {
            with(videoPlayer) {
                setSurfaceHolder(surfaceHolder)
                setCallback(videoPlayerCallback)
                setScaleType(PlayerScaleType.CENTER_INSIDE)
                setVideoSize(Size(1024, 768))
                setVolume(1f)
            }
        } else {
            _errorMsg.value = "Error while prepare video editor player"
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

    fun applyFxEffect() {
        val fxEffect = generateFxEffect()
        appliedEffects.add(fxEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeFxEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.VISUAL }
        videoPlayer.setEffects(appliedEffects)
    }

    fun applyTextEffect() {
        val textEffect = createTextVisualEffect()
        appliedEffects.add(textEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeTextEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.TEXT }
        videoPlayer.setEffects(appliedEffects)
    }

    fun applyGifEffect() {
        val gifEffect = createGifVisualEffect()
        appliedEffects.add(gifEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeGifEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.GIF }
        videoPlayer.setEffects(appliedEffects)
    }

    fun applySpeedEffect() {
        val rapidEffect = createRapidEffect()
        appliedEffects.add(rapidEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeSpeedEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.TIME }
        videoPlayer.setEffects(appliedEffects)
    }

    fun applyLutEffect() {
        val lutEffect = createColorFilterEffect()
        appliedEffects.add(lutEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeLutEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.COLOR }
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeMusicEffect() {
        videoPlayer.setMusicEffects(emptyList())
    }

    fun applyCustomEffect() {
        val customEffect = generateCustomEffect()
        appliedEffects.add(customEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun remoteCustomEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.CUSTOM }
        videoPlayer.setEffects(appliedEffects)
    }

    fun releasePlayer(surfaceHolder: SurfaceHolder) {
        videoPlayer.clearSurfaceHolder(surfaceHolder)
        videoPlayer.release()
    }

    fun takeScreenshot() {
        videoPlayer.takeScreenshot()
    }

    /**
     * Creates fx effect.
     * To get full list of fx effects, check classes of BaseVisualEffectDrawable type.
     * By default each fx effect applied on the whole video duration.
     */
    private fun generateFxEffect(): VisualTimedEffect {
        val fxDrawable = VideoEffectsHelper.createFxEffect(
            context = context,
            resourceIdentifier = "vhs"
        ) ?: throw Exception("Video effect is not available!")
        return VisualTimedEffect(effectDrawable = fxDrawable)
    }

    /**
     * Creates a text effect. The text is created using a canvas and converted to a bitmap.
     * RectParams are used to set the coordinates, size, scale and rotation of the effect.
     * To use relative coordinates you can use GLViewPort object taken from
     * onViewPortChanged callback.
     */
    private fun createTextVisualEffect(): VisualTimedEffect {
        val bitmap = Bitmap.createBitmap(800, 150, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = 64f
        canvas.drawText("I am a Text!", 0f, 60f, paint)

        val rectParams = RectParams().apply {
            setCoordinates(25f, 25f, bitmap.width.toFloat(), bitmap.height.toFloat(), 0.8f, 0f)
        }

        return VisualTimedEffect(
            effectDrawable = VideoEffectsHelper.createTextEffect(UUID.randomUUID(), bitmap, rectParams)
        )
    }

    /**
     * Creates a gif(sticker) effect. Gif file must be downloaded to be used as an effect.
     * The code below uses gif from assets.
     * RectParams are used to set the coordinates, size, scale and rotation of the bitmap.
     * To use relative coordinates you can use GLViewPort object taken from
     * onViewPortChanged callback.
     */
    private fun createGifVisualEffect(): VisualTimedEffect {
        val stickerUri = context.copyFromAssetsToExternal("example.gif").toUri()

        val rectParams = RectParams().apply {
            setCoordinates(200f, 350f, 361f, 277f, 1f, 20f)
        }

        return VisualTimedEffect(
            effectDrawable = VideoEffectsHelper.createGifEffect(UUID.randomUUID(), stickerUri, rectParams)
        )
    }

    /**
     * Creates Rapid speed effect.
     * By default each speed effect applied on the whole video duration.
     */
    private fun createRapidEffect(): SpeedTimedEffect {
        val speedEffect = VideoEffectsHelper.createSpeedEffect(2F)
        return SpeedTimedEffect(effectDrawable = speedEffect)
    }

    private fun createColorFilterEffect(): VisualTimedEffect {
        val colorEffectFile = context.copyFromAssetsToExternal("color_filter_example.png")
        return VisualTimedEffect(VideoEffectsHelper.createLutEffect(colorEffectFile.path, Size(1024, 768)))
    }

    /**
     * Creates custom effect.
     */
    private fun generateCustomEffect(): VisualTimedEffect {
        val (width, height) = viewportSize.width to viewportSize.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawCircle(width / 2.0f, height / 2.0f, height / 5.0f, paint)
        return VisualTimedEffect(effectDrawable = CustomEffectDrawable(bitmap))
    }

    data class PlaybackMusicEffect(
        override val uuid: ParcelUuid,
        override val sourceUri: Uri,
        override val startOnTimelineMs: Long,
        override val startOnSourceMs: Long,
        override val effectDurationMs: Long,
        override val normalSpeedEffectDurationMs: Long,
        override val volume: Float,
        override val playUri: Uri,
        override val equalizerEffect: EqualizerEffect?,
        override val fadeEffect: FadeEffect
    ) : MusicEffect
}