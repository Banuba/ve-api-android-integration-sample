package com.banuba.example.videoeditor.export

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import androidx.annotation.CallSuper
import com.banuba.sdk.core.effects.EffectRenderer
import com.banuba.sdk.core.gl.BnBGLUtils
import java.nio.FloatBuffer

class CustomEffectRenderer(
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
