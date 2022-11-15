package com.banuba.example.videoeditor.export

import android.graphics.Bitmap
import android.util.Size
import com.banuba.sdk.core.effects.DrawType
import com.banuba.sdk.core.effects.EffectRenderer
import com.banuba.sdk.core.effects.RenderParamsProvider
import com.banuba.sdk.core.effects.VisualEffectDrawable
import java.util.UUID

class CustomEffectDrawable(private val bitmap: Bitmap) : VisualEffectDrawable {

    private val mUuid = UUID.randomUUID()

    override fun getType() = DrawType.CUSTOM

    override fun getUuid(): UUID = mUuid

    override fun getCacheKey(): String = javaClass.name

    override fun createEffectRenderer(drawSize: Size): EffectRenderer {
        return CustomEffectRenderer(bitmap, drawSize, 40)
    }

    override fun getRenderParamsProvider(): RenderParamsProvider = RenderParamsProvider { null }
}