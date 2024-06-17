package com.banuba.example.videoeditor.utils

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException
/**
 * Banuba Face AR EffectPlayer can only apply effects stored on the internal memory of the device.
 * This util class prepares AR effect before applying in Banuba Face AR EffectPlayer.
 * You can use this implementation it in your project.
 */
class BanubaEffectHelper(context: Context) {

    companion object {
        const val TAG = "BanubaEffectHelper"

        private const val DIR_EFFECTS = "effects"
        private const val DIR_BNB_RESOURCES = "bnb-resources"
        private const val assetsEffectsDir = "$DIR_BNB_RESOURCES/$DIR_EFFECTS"
    }
    private val assetManager: AssetManager = context.assets
    private val resourcesPath: String = context.filesDir.absolutePath

    // Copied Face AR effect from assets to local device storage.
    fun prepareEffect(
        assetEffectName: String
    ): Effect {
        val effectUri = Uri.parse(resourcesPath)
            .buildUpon()
            .appendPath(DIR_BNB_RESOURCES)
            .appendPath(DIR_EFFECTS)
            .appendPath(assetEffectName)
            .build()

        val file = File(effectUri.toString())
        copyResources(
            file,
            "$assetsEffectsDir/$assetEffectName"
        )
        val uri = Uri.fromFile(file)
        val previewImagePath = uri
            .buildUpon()
            .appendPath("preview.png")
            .build()

        return Effect(effectUri, assetEffectName, previewImagePath)
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
                    copyFile(assetManager, sourcePath, destFile)
                }
            }
        } else {
            copyFile(assetManager, assetRoot, targetDir)
        }
    }

    private fun copyFile(
        assetManager: AssetManager,
        sourcePath: String,
        desFile: File
    ) {
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

    data class Effect(
        val uri: Uri,
        val name: String,
        val previewImageUri: Uri
    )

    private fun AssetManager.isDirectory(path: String): Boolean = try {
        list(path)?.isNotEmpty() ?: false
    } catch (e: IOException) {
        Log.w(TAG, e)
        false
    }
}