package com.banuba.example.videoeditor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.banuba.example.videoeditor.camera.CameraActivity
import com.banuba.example.videoeditor.databinding.ActivitySampleBinding

class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startMainFlowButton.setOnClickListener {
            startActivity(Intent(applicationContext, CameraActivity::class.java))
        }
    }
}