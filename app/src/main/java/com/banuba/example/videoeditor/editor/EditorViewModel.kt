package com.banuba.example.videoeditor.editor

import android.app.Application
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
import androidx.lifecycle.*
import com.banuba.example.videoeditor.export.CustomEffectDrawable
import com.banuba.sdk.core.data.MediaDataGalleryValidator
import com.banuba.sdk.core.data.MediaValidationResultType
import com.banuba.sdk.core.domain.AspectRatioProvider
import com.banuba.sdk.core.effects.*
import com.banuba.sdk.core.ext.copyFromAssetsToExternal
import com.banuba.sdk.core.gl.GlViewport
import com.banuba.sdk.core.media.DurationExtractor
import com.banuba.sdk.effects.ve.VideoEffectsHelper
import com.banuba.sdk.export.data.ExportFlowManager
import com.banuba.sdk.export.data.ExportResult
import com.banuba.sdk.export.data.ExportTaskParams
import com.banuba.sdk.playback.PlaybackError
import com.banuba.sdk.playback.PlayerScaleType
import com.banuba.sdk.playback.VideoPlayer
import com.banuba.sdk.ve.domain.TimeBundle
import com.banuba.sdk.ve.domain.VideoRangeList
import com.banuba.sdk.ve.domain.VideoRecordRange
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.SpeedTimedEffect
import com.banuba.sdk.ve.effects.TypedTimedEffect
import com.banuba.sdk.ve.effects.VisualTimedEffect
import com.banuba.sdk.ve.effects.music.MusicEffect
import com.banuba.sdk.ve.ext.VideoEditorUtils
import com.banuba.sdk.ve.ext.setCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EditorViewModel(
    private val appContext: Application,
    private val videoValidator: MediaDataGalleryValidator,
    private val videoPlayer: VideoPlayer,
    private val exportFlowManager: ExportFlowManager,
    private val aspectRatioProvider: AspectRatioProvider
) : AndroidViewModel(appContext) {

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String>
        get() = _errorMsg

    private var exportVideosList = mutableListOf<VideoRecordRange>()
    private var exportMusicList = mutableListOf<MusicEffect>()

    val exportResultData: LiveData<ExportResult> = MediatorLiveData<ExportResult>().apply {
        addSource(exportFlowManager.resultData) { exportResult ->
            value = exportResult
        }
    }

    /**
     * This list is used to store applied visual and speed effects
     */
    private val appliedEffects = mutableListOf<TypedTimedEffect<*>>()

    private val videoPlayerCallback = object : VideoPlayer.Callback {
        override fun onScreenshotTaken(bmp: Bitmap) {
        }

        override fun onVideoPlaybackError(error: PlaybackError) {
            _errorMsg.postValue(error.toString())
        }

        override fun onVideoPositionChanged(positionMs: Int) {
        }

        override fun onViewportChanged(viewport: GlViewport) {
            Log.d(
                "BanubaVideoPlayer",
                "Video view port: (${viewport.x}, ${viewport.y}), Size(${viewport.width}x${viewport.height})"
            )
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
                .mapNotNull {
                    VideoEditorUtils
                        .createVideoRecordRange(
                            sourceUri = it,
                            context =  appContext,
                            pipApplied = false
                        )
                }
            videoPlayer.setVideoRanges(playlist)
            exportVideosList.addAll(playlist)
            play()
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
                DurationExtractor(video = false).extractDurationMilliSec(appContext, uri)
            val musicEffect = PlaybackMusicEffect(
                uuid = ParcelUuid(UUID.randomUUID()),
                sourceUri = uri,
                startOnTimelineMs = 0,
                startOnSourceMs = 0,
                effectDurationMs = effectDuration,
                normalSpeedEffectDurationMs = effectDuration,
                volume = 1f,
                playUri = uri,
                equalizerEffect = null
            )
            videoPlayer.setMusicEffects(listOf(musicEffect))
            exportMusicList.add(musicEffect)
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
        if (videoPlayer.prepare(Size(720, 1280))) {
            with(videoPlayer) {
                setSurfaceHolder(surfaceHolder)
                setCallback(videoPlayerCallback)
                setScaleType(PlayerScaleType.FIT_SCREEN_HEIGHT)
                setVideoSize(Size(720, 1280))
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

    fun applyCustomEffect() {
        val customEffect = generateCustomEffect()
        appliedEffects.add(customEffect)
        videoPlayer.setEffects(appliedEffects)
    }

    fun removeCustomEffect() {
        appliedEffects.removeAll { it.drawable.type == DrawType.CUSTOM }
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
        exportMusicList.clear()
    }

    fun releasePlayer(surfaceHolder: SurfaceHolder) {
        videoPlayer.clearSurfaceHolder(surfaceHolder)
        videoPlayer.release()
    }

    fun startExport() {
        val visualStack = Stack<VisualTimedEffect>().apply {
            addAll(appliedEffects.filterIsInstance<VisualTimedEffect>())
        }
        val speedStack = Stack<SpeedTimedEffect>().apply {
            addAll(appliedEffects.filterIsInstance<SpeedTimedEffect>())
        }

        val exportTaskParams = ExportTaskParams(
            videoRanges = VideoRangeList(exportVideosList),
            effects = Effects(visualStack, speedStack),
            musicEffects = exportMusicList,
            videoVolume = 1F,
            coverFrameSize = Size(720, 1280),
            aspect = aspectRatioProvider.provide()        //by default provided aspect ratio = 9.0 / 16
        )

        exportFlowManager.startExport(exportTaskParams)
    }

    /**
     * Creates fx effect.
     * To get full list of fx effects, check classes of BaseVisualEffectDrawable type.
     * By default each fx effect applied on the whole video duration.
     */
    private fun generateVHSEffect(): VisualTimedEffect {
        val vhsDrawable = VideoEffectsHelper.takeAvailableFxEffects(appContext).find {
            appContext.getString(it.nameRes) == "VHS"
        }?.provide() ?: throw Exception("VHS video effect is not available!")
        if (vhsDrawable !is VisualEffectDrawable) throw TypeCastException("Drawable is not IVisualEffectDrawable type!")
        return VisualTimedEffect(effectDrawable = vhsDrawable)
    }

    /**
     * Creates fx effect.
     * To get full list of fx effects, check classes of BaseVisualEffectDrawable type.
     * By default each fx effect applied on the whole video duration.
     */
    private fun generateFxEffect(): VisualTimedEffect {
        val fxDrawable = VideoEffectsHelper.createFxEffect(
            context = appContext,
            resourceIdentifier = "vhs"
        ) ?: throw Exception("Video effect is not available!")
        return VisualTimedEffect(effectDrawable = fxDrawable)
    }

    /**
     * Creates custom effect.
     */
    private fun generateCustomEffect(): VisualTimedEffect {
        val (width, height) = 720 to 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawCircle(width / 2.0f, height / 2.0f, height / 5.0f, paint)
        return VisualTimedEffect(effectDrawable = CustomEffectDrawable(bitmap))
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
        val stickerUri = appContext.copyFromAssetsToExternal("example.gif").toUri()

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
        return SpeedTimedEffect(
            effectDrawable = speedEffect,
            endTimeBundle = TimeBundle(exportVideosList.size - 1, Int.MAX_VALUE)
        )
    }

    private fun createColorFilterEffect(): VisualTimedEffect {
        val colorEffectFile = appContext.copyFromAssetsToExternal("color_filter_example.png")
        return VisualTimedEffect(VideoEffectsHelper.createLutEffect(colorEffectFile.path, Size(1024, 768)))
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
        override val fadeEffect: FadeEffect = FadeEffect.EMPTY
    ) : MusicEffect
}