package com.banuba.example.videoeditor

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.ParcelUuid
import android.util.Size
import androidx.core.net.toUri
import com.banuba.example.videoeditor.export.CustomEffectDrawable
import com.banuba.sdk.core.effects.EqualizerEffect
import com.banuba.sdk.core.effects.FadeEffect
import com.banuba.sdk.core.effects.RectParams
import com.banuba.sdk.core.effects.VisualEffectDrawable
import com.banuba.sdk.core.ext.copyFromAssetsToExternal
import com.banuba.sdk.effects.ve.VideoEffectsHelper
import com.banuba.sdk.ve.domain.TimeBundle
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.SpeedTimedEffect
import com.banuba.sdk.ve.effects.VisualTimedEffect
import com.banuba.sdk.ve.effects.music.MusicEffect
import java.util.*

object SampleEffectsProvider {

    // Creates visual FX effect that is applied for the whole video.
    fun createFxEffect(
        context: Context,
        fxName: String
    ): VisualTimedEffect {
        val availableList = VideoEffectsHelper.takeAvailableFxEffects(context)
        val vhsDrawable = availableList.find {
            context.getString(it.nameRes) == fxName
        }?.provide() ?: throw Exception("VHS video effect is not available!")

        if (vhsDrawable !is VisualEffectDrawable) throw TypeCastException("Drawable is not IVisualEffectDrawable type!")

        return VisualTimedEffect(effectDrawable = vhsDrawable)
    }

    /**
     * Creates a sticker(gif) effect. Gif file must be downloaded to be used as an effect.
     * The code below uses gif from assets.
     * RectParams are used to set the coordinates, size, scale and rotation of the bitmap.
     */
    fun createStickerEffect(context: Context): VisualTimedEffect {
        val stickerUri = context.copyFromAssetsToExternal("example.gif").toUri()
        val rectParams = RectParams().apply {
            setCoordinates(100f, 100f, 361f, 277f, 1f, 20f)
        }

        return VisualTimedEffect(
            effectDrawable = VideoEffectsHelper.createGifEffect(
                UUID.randomUUID(),
                stickerUri,
                rectParams
            )
        )
    }

    // Creates Rapid speed effect that is applied for a specific amount of time
    fun createRapidSpeedEffect(videoDuration: Long): SpeedTimedEffect {
        val videoMid = videoDuration.toInt() / 2
        val speedEffect = VideoEffectsHelper.createSpeedEffect(2F)
        return SpeedTimedEffect(
            effectDrawable = speedEffect,
            startTimeBundle = TimeBundle(0, 0),
            startTotal = 0,
            endTimeBundle = TimeBundle(0, videoMid),
            endTotal = videoMid
        )
    }

    // Creates SlowMotion speed effect that is applied for a specific amount of time
    fun createSlowMotionSpeedEffect(videoDuration: Long): SpeedTimedEffect {
        val videoMid = videoDuration.toInt() / 2
        val speedEffect = VideoEffectsHelper.createSpeedEffect(0.5F)
        return SpeedTimedEffect(
            effectDrawable = speedEffect,
            startTimeBundle = TimeBundle(0, videoMid),
            startTotal = videoMid,
            endTimeBundle = TimeBundle(0, videoDuration.toInt()),
            endTotal = videoDuration.toInt()
        )
    }

    // Creates color filter that is applied for the whole video
    fun createColorFilterEffect(context: Context): VisualTimedEffect {
        val colorEffectFile = context.copyFromAssetsToExternal("color_filter_example.png")
        return VisualTimedEffect(
            VideoEffectsHelper.createLutEffect(
                colorEffectFile.path,
                Size(1024, 768)
            )
        )
    }

    /**
     * Creates custom effect.
     */
    fun createCustomEffect(viewportSize: Size): VisualTimedEffect {
        val (width, height) = viewportSize.width to viewportSize.height
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
    fun createTextVisualEffect(text: String): VisualTimedEffect {
        val bitmap = Bitmap.createBitmap(800, 150, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = 64f
        canvas.drawText(text, 0f, 60f, paint)

        val rectParams = RectParams().apply {
            setCoordinates(25f, 25f, bitmap.width.toFloat(), bitmap.height.toFloat(), 0.8f, 0f)
        }

        return VisualTimedEffect(
            effectDrawable = VideoEffectsHelper.createTextEffect(
                UUID.randomUUID(),
                bitmap,
                rectParams
            )
        )
    }

    private fun RectParams.setCoordinates(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        scale: Float,
        rotation: Float
    ) {
        val points = floatArrayOf(
            x, y,
            x + width, y,
            x, y + height,
            x + width, y + height
        )
        Matrix().apply {
            reset()
            postScale(scale, scale, x + width / 2, y + height / 2)
            postRotate(rotation, x + width / 2, y + height / 2)
            mapPoints(points)
        }
        set(
            points[0], points[1],
            points[2], points[3],
            points[4], points[5],
            points[6], points[7]
        )
    }

    /**
     * Creates a few effects to apply in export.
     */
    fun provideExportEffects(
        context: Context,
        totalVideoDuration: Long
    ): Effects {
        val effectText = createTextVisualEffect("Text in Export")
        val effectGif = createStickerEffect(context)
        val effectFx = createFxEffect(context, "VHS")

        // Visual effects i.e. VHS, Glitch are not fully supported yet
        val visualStack = Stack<VisualTimedEffect>().apply {
            add(effectText)
            add(effectGif)
            add(effectFx)
        }

        val rapidEffect = createRapidSpeedEffect(totalVideoDuration)
        val slowMotionEffect = createSlowMotionSpeedEffect(totalVideoDuration)

        //Use empty stack because speed effects are not fully supported yet.
        val empty = Stack<SpeedTimedEffect>().apply {
            add(rapidEffect)
            add(slowMotionEffect)
        }

        return Effects(
            visualStack = visualStack,
            speedStack = empty
        )
    }

    data class MusicTrackEffect(
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