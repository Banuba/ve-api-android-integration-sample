package com.banuba.example.videoeditor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.banuba.example.videoeditor.camera.CameraActivity
import com.banuba.example.videoeditor.databinding.ActivitySampleBinding
import com.banuba.example.videoeditor.export.ExportActivity
import com.banuba.example.videoeditor.playback.PlaybackActivity
import com.banuba.sdk.token.storage.license.LicenseStateCallback

class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startMainFlowButton.setOnClickListener {
            checkVideoEditorLicense {
                startActivity(Intent(applicationContext, CameraActivity::class.java))
            }
        }

        binding.startExportFlowButton.setOnClickListener {
            checkVideoEditorLicense {
                startActivity(Intent(applicationContext, ExportActivity::class.java))
            }
        }

        binding.startPlaybackFlowButton.setOnClickListener {
            checkVideoEditorLicense {
                startActivity(Intent(applicationContext, PlaybackActivity::class.java))
            }
        }
    }


    private fun checkVideoEditorLicense(
        startActivity: () -> Unit,
    ) {
        val videoEditor = (application as SampleApp).videoEditor
        if (videoEditor == null) {
            Log.e(
                "BanubaVideoEditor",
                "Cannot check license state. Please initialize Video Editor SDK"
            )
            showToast(SampleApp.ERR_SDK_NOT_INITIALIZED)
        } else {
            // Checking the license might take around 1 sec in the worst case.
            // Please optimize use if this method in your application for the best user experience
            videoEditor.getLicenseState { isValid ->
                if (isValid) {
                    // ✅ License is active, all good
                    // You can show button that opens Video Editor or
                    // Start Video Editor right away
                    startActivity()
                } else {
                    // ❌ Use of Video Editor is restricted. License is revoked or expired.
                    showToast(SampleApp.ERR_LICENSE_REVOKED)
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
    }
}