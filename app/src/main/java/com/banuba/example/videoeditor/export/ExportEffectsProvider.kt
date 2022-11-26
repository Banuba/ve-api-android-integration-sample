package com.banuba.example.videoeditor.export

import android.content.Context
import android.graphics.*
import androidx.core.net.toUri
import com.banuba.sdk.core.effects.VisualEffectDrawable
import com.banuba.sdk.core.effects.RectParams
import com.banuba.sdk.core.ext.copyFromAssetsToExternal
import com.banuba.sdk.effects.ve.VideoEffectsHelper
import com.banuba.sdk.ve.domain.TimeBundle
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.SpeedTimedEffect
import com.banuba.sdk.ve.effects.VisualTimedEffect
import java.util.*

class ExportEffectsProvider {
    /**
     * Creates a few effects that are applied to the entire duration of video in export.
     */
    fun provideEffects(
        context: Context,
        totalVideoDuration: Long
    ): Effects {
        val effectText = createTextVisualEffect()
        val effectGif = createGifVisualEffect(context)
        val effectFx = generateFxEffect(context)

        // Visual effects i.e. VHS, Glitch are not fully supported yet
        val visualStack = Stack<VisualTimedEffect>().apply {
            add(effectText)
            add(effectGif)
            add(effectFx)
        }

        val rapidEffect = createRapidEffect(totalVideoDuration)
        val slowMotionEffect = createSlowMotionEffect(totalVideoDuration)

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


    /**
     * Creates fx effect.
     * To get full list of fx effects, check classes of BaseVisualEffectDrawable type.
     */
    private fun generateFxEffect(context: Context): VisualTimedEffect {
        val vhsDrawable = VideoEffectsHelper.takeAvailableFxEffects(context).find {
            context.getString(it.nameRes) == "VHS"
        }?.provide() ?: throw Exception("VHS video effect is not available!")
        if (vhsDrawable !is VisualEffectDrawable) throw TypeCastException("Drawable is not IVisualEffectDrawable type!")
        return VisualTimedEffect(effectDrawable = vhsDrawable)
    }

    /**
     * Creates a gif(sticker) effect. Gif file must be downloaded to be used as an effect.
     * The code below uses gif from assets.
     * RectParams are used to set the coordinates, size, scale and rotation of the bitmap.
     */
    private fun createGifVisualEffect(context: Context): VisualTimedEffect {
        val stickerUri = context.copyFromAssetsToExternal("example.gif").toUri()

        val rectParams = RectParams().apply {
            setCoordinates(200f, 700f, 361f, 277f, 1f, 20f)
        }

        return VisualTimedEffect(
            effectDrawable = VideoEffectsHelper.createGifEffect(
                UUID.randomUUID(),
                stickerUri,
                rectParams
            )
        )
    }

    /**
     * Creates Rapid speed effect from the beginning to the middle of the video
     */
    private fun createRapidEffect(videoDuration: Long): SpeedTimedEffect {
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

    /**
     * Creates SlowMotion speed effect from the middle to the end of the video
     */
    private fun createSlowMotionEffect(videoDuration: Long): SpeedTimedEffect {
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


    /**
     * Creates a text effect. The text is created using a canvas and converted to a bitmap.
     * RectParams are used to set the coordinates, size, scale and rotation of the effect.
     */
    private fun createTextVisualEffect(): VisualTimedEffect {
        val bitmap = Bitmap.createBitmap(800, 150, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 64f
        canvas.drawText("Hello, World!", 0f, 60f, paint)

        val rectParams = RectParams().apply {
            setCoordinates(150f, 300f, bitmap.width.toFloat(), bitmap.height.toFloat(), 0.8f, 0f)
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
}