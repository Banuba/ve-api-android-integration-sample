package com.banuba.example.videoeditor.camera

import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import com.banuba.sdk.core.ext.isDirectory
import java.io.File
import java.io.IOException

class EffectLoaderManager(
    private val assetManager: AssetManager
)  {

    companion object {
        const val TAG = "EffectsResourceManager"

        const val DIR_EFFECTS = "effects"
    }

    val assetsEffectsDir = "bnb-resources/$DIR_EFFECTS"

    fun prepareEffect(effectUri: String, assetEffectsName: String): Effect {
        val file = File(effectUri)
        copyResources(
            file,
            "$assetsEffectsDir/$assetEffectsName"
        )
        val uri = Uri.fromFile(file)
        val previewImagePath = uri
            .buildUpon()
            .appendPath("preview.png")
            .build()

        return Effect(uri, assetEffectsName, previewImagePath)
    }

    private fun copyResources(
        targetDir: File,
        assetRoot: String
    ) {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        if (assetManager.isDirectory(assetRoot)) {
            assetManager.list(assetRoot)?.forEach { filename ->
                val sourcePath = Uri.parse(assetRoot)
                    .buildUpon()
                    .appendEncodedPath(filename)
                    .build()
                    .path ?: throw IllegalStateException("Source path cannot be null!")

                val destFile = File(targetDir, filename)

                if (assetManager.isDirectory(sourcePath)) {
                    destFile.mkdirs()
                    copyResources(
                        targetDir = destFile,
                        assetRoot = sourcePath
                    )
                } else {
                    copyFile(sourcePath, destFile)
                }
            }
        } else {
            copyFile(assetRoot, targetDir)
        }
    }

    private fun copyFile(sourcePath: String, desFile: File) {
        try {
            assetManager.open(sourcePath).use { input ->
                desFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Could not copy file $sourcePath")
        }
    }
}