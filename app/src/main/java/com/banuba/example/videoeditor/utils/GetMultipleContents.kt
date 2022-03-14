package com.banuba.example.videoeditor.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class GetMultipleContents : ActivityResultContract<String, List<Uri>>() {

    private var context: Context? = null

    override fun createIntent(context: Context, input: String): Intent {
        this.context = context
        return Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(input)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return if (intent == null || resultCode != Activity.RESULT_OK) {
            emptyList()
        } else {
            getClipDataUris(intent).takePersistableUris()
        }
    }

    private fun getClipDataUris(intent: Intent): List<Uri> {
        val resultSet = linkedSetOf<Uri>()
        intent.data?.let { resultSet.add(it) }
        val clipData = intent.clipData ?: return resultSet.toList()
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri ?: continue
            resultSet.add(uri)
        }
        return resultSet.toList()
    }

    private fun List<Uri>.takePersistableUris() = onEach {
        runCatching {
            context?.contentResolver?.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }
}