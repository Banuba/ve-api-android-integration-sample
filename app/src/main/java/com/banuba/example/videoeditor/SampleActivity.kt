package com.banuba.example.videoeditor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.banuba.example.videoeditor.databinding.ActivitySampleBinding
import com.banuba.example.videoeditor.export.ExportActivity
import com.banuba.example.videoeditor.playback.PlaybackActivity

class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.exportSampleButton.setOnClickListener {
            checkVideoEditorLicenseState {
                startActivity(Intent(applicationContext, ExportActivity::class.java))
            }
        }

        binding.playbackSampleButton.setOnClickListener {
            checkVideoEditorLicenseState {
                startActivity(Intent(applicationContext, PlaybackActivity::class.java))
            }
        }
    }

    private fun checkVideoEditorLicenseState(
        onActiveLicenseBlock: () -> Unit,
    ) {
        val videoEditor = (application as SampleApp).videoEditor
        if (videoEditor == null) {
            Log.e(
                "BanubaVideoEditor",
                "Cannot check license state. Please initialize Video Editor SDK"
            )
            showLicenseErrorMessage(SampleApp.ERR_SDK_NOT_INITIALIZED)
        } else {
            // Checking the license might take around 1 sec in the worst case.
            // Please optimize use if this method in your application for the best user experience
            videoEditor.getLicenseState { isValid ->
                if (isValid) {
                    // ✅ License is active, all good
                    // You can show button that opens Video Editor or
                    // Start Video Editor right away
                    onActiveLicenseBlock()
                } else {
                    // ❌ Use of Video Editor is restricted. License is revoked or expired.
                    showLicenseErrorMessage(SampleApp.ERR_LICENSE_REVOKED)
                }
            }
        }
    }

    private fun showLicenseErrorMessage(msg: String) {
        binding.licenseErrorMessageView.run {
            text = msg
            visibility = View.VISIBLE
        }
    }
}