package com.banuba.example.videoeditor

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.opengl.GLES20
import android.os.ParcelUuid
import android.util.Size
import androidx.annotation.CallSuper
import androidx.core.net.toUri
import com.banuba.sdk.core.effects.*
import com.banuba.sdk.core.ext.copyFromAssetsToExternal
import com.banuba.sdk.core.gl.BnBGLUtils
import com.banuba.sdk.effects.ve.VideoEffectsHelper
import com.banuba.sdk.ve.domain.TimeBundle
import com.banuba.sdk.ve.effects.Effects
import com.banuba.sdk.ve.effects.SpeedTimedEffect
import com.banuba.sdk.ve.effects.VisualTimedEffect
import com.banuba.sdk.ve.effects.music.MusicEffect
import java.nio.FloatBuffer
import java.util.*

object SampleEffectsProvider {

    // Creates visual FX effect that is applied for the whole video.
    fun createFxEffect(
        context: Context,
        fxName: String
    ): VisualTimedEffect {
        val availableList = VideoEffectsHelper.provideVisualEffects(context)
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
     * Creates blur effect.
     */
    fun createBlurEffect(viewportSize: Size): VisualTimedEffect {
        val (width, height) = viewportSize.width to viewportSize.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawCircle(width / 2.0f, height / 2.0f, height / 5.0f, paint)
        return VisualTimedEffect(effectDrawable = BlurEffectDrawable(bitmap))
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

private class BlurEffectDrawable(private val bitmap: Bitmap) : VisualEffectDrawable {

    private val mUuid = UUID.randomUUID()

    override fun getType() = DrawType.CUSTOM

    override fun getUuid(): UUID = mUuid

    override fun getCacheKey(): String = javaClass.name

    override fun createEffectRenderer(drawSize: Size): EffectRenderer {
        return CustomEffectRenderer(bitmap, drawSize, 40)
    }

    override fun getRenderParamsProvider(): RenderParamsProvider = RenderParamsProvider { null }
}

private class CustomEffectRenderer(
    bitmap: Bitmap,
    private val drawSize: Size,
    private val pixelSquareSize: Int
) : EffectRenderer {

    companion object {
        private const val DEPTH = 0.0f
        private val RECTANGLE_VERTEX = floatArrayOf(
            -1.0f, -1.0f, DEPTH, // 0 bottom left
            1.0f, -1.0f, DEPTH, // 1 bottom right
            -1.0f, 1.0f, DEPTH, // 2 top left
            1.0f, 1.0f, DEPTH // 3 top right
        )

        private val RECTANGLE_TEXTURE = floatArrayOf(
            0.0f, 0.0f, // 0 bottom left
            1.0f, 0.0f, // 1 bottom right
            0.0f, 1.0f, // 2 top left
            1.0f, 1.0f // 3 top right
        )

        private const val VERTEX_SHADER = """
            uniform mat4 uTexMatrix;
            attribute vec4 a_position;
            attribute vec2 a_texCoord;
            varying vec2 v_texCoord;
            void main()
            {
             gl_Position = a_position;
             vec4 texCoord = vec4(a_texCoord, 0.0, 1.0);
             v_texCoord = (uTexMatrix * texCoord).xy;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 v_texCoord;
            uniform vec2 resolution;
            uniform float squareSize;
            uniform sampler2D s_baseMap;
            uniform sampler2D samplerMask;
            void main()
            {
                highp vec2 texture2screen = resolution / squareSize;          
                highp vec2 screen2texture = squareSize / resolution;          
                vec2 screenGrid0 = floor(v_texCoord * texture2screen);        
                vec2 newUV0 = screenGrid0 * screen2texture;                   
                vec2 newUV1 = (screenGrid0 + vec2(0.0, 1.0)) * screen2texture;
                vec2 newUV2 = (screenGrid0 + vec2(1.0, 1.0)) * screen2texture;
                vec2 newUV3 = (screenGrid0 + vec2(1.0, 0.0)) * screen2texture;
                float mask =  texture2D(samplerMask, newUV0).r +              
                              texture2D(samplerMask, newUV1).r +              
                              texture2D(samplerMask, newUV2).r +              
                              texture2D(samplerMask, newUV3).r;               
                if (mask >= 0.5) {                                            
                     vec4 color = texture2D(s_baseMap, newUV0) +              
                                  texture2D(s_baseMap, newUV1) +              
                                  texture2D(s_baseMap, newUV2) +              
                                  texture2D(s_baseMap, newUV3);               
                     gl_FragColor = color / 4.0;                              
                 } else {                                                     
                     gl_FragColor = texture2D(s_baseMap, v_texCoord);         
                };    
            }
        """
    }

    private val maskTextureID: Int = BnBGLUtils.createTextureObject()

    private val vertexCount = RECTANGLE_VERTEX.size / BnBGLUtils.COORDS_PER_VERTEX
    private var programHandle = 0
    private var attributePosition = 0
    private var attributeTextureCoord = 0
    private var uniformSampler = 0
    private var uniformTextureMatrix = 0
    private val identityMatrix = BnBGLUtils.getIdentityMatrix()
    private var resolution = 0
    private var squareSize = 0
    private var uniformMask = 0

    private val vbo = IntArray(2).apply {
        GLES20.glGenBuffers(size, this, 0)
        BnBGLUtils.loadBufferData(this[0], RECTANGLE_VERTEX)
        BnBGLUtils.loadBufferData(this[1], RECTANGLE_TEXTURE)
    }

    init {
        programHandle = BnBGLUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        // Vertex shader
        attributePosition = GLES20.glGetAttribLocation(programHandle, "a_position")
        attributeTextureCoord = GLES20.glGetAttribLocation(programHandle, "a_texCoord")

        uniformTextureMatrix = GLES20.glGetUniformLocation(programHandle, "uTexMatrix")

        // Fragment Shader
        uniformSampler = GLES20.glGetUniformLocation(programHandle, "s_baseMap")
        uniformMask = GLES20.glGetUniformLocation(programHandle, "samplerMask")

        resolution = GLES20.glGetUniformLocation(programHandle, "resolution")
        squareSize = GLES20.glGetUniformLocation(programHandle, "squareSize")

        BnBGLUtils.loadTexture1ch(maskTextureID, bitmap, false)

        BnBGLUtils.checkGlError("LOAD")
    }

    override fun render(
        input: Int,
        output: Int,
        params: FloatBuffer?,
        timeLocalSec: Float,
        timeGlobalSec: Float
    ) {
        GLES20.glViewport(0, 0, drawSize.width, drawSize.height)
        GLES20.glUseProgram(programHandle)

        // Vertex Shader Buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glVertexAttribPointer(
            attributePosition,
            BnBGLUtils.COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            BnBGLUtils.VERTEX_STRIDE,
            0
        )
        GLES20.glEnableVertexAttribArray(attributePosition)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
        GLES20.glVertexAttribPointer(
            attributeTextureCoord,
            BnBGLUtils.COORDS_UV_PER_TEXTURE,
            GLES20.GL_FLOAT,
            false,
            BnBGLUtils.TEXTURE_STRIDE,
            0
        )
        GLES20.glEnableVertexAttribArray(attributeTextureCoord)

        // Vertex Shader - Uniforms
        GLES20.glUniformMatrix4fv(uniformTextureMatrix, 1, false, identityMatrix, 0)

        GLES20.glUniform1f(squareSize, pixelSquareSize.toFloat())
        GLES20.glUniform2f(
            resolution,
            drawSize.width.toFloat(),
            drawSize.height.toFloat()
        )

        // Fragment Shader - Texture
        BnBGLUtils.setupSampler(0, uniformSampler, input, false)
        BnBGLUtils.setupSampler(1, uniformMask, maskTextureID, false)

        // Drawing
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)

        // Clearing
        GLES20.glDisableVertexAttribArray(attributePosition)
        GLES20.glDisableVertexAttribArray(attributeTextureCoord)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glUseProgram(0)
    }

    @CallSuper
    override fun release() {
        GLES20.glDeleteTextures(1, intArrayOf(maskTextureID), 0)
        GLES20.glDeleteProgram(programHandle)
        GLES20.glDeleteBuffers(vbo.size, vbo, 0)
    }
}