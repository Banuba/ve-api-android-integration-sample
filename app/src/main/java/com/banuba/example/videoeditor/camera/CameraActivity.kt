package com.banuba.example.videoeditor.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.banuba.example.videoeditor.R
import com.banuba.example.videoeditor.databinding.ActivityCameraBinding
import com.banuba.example.videoeditor.editor.EditorActivity
import com.banuba.example.videoeditor.utils.GetMultipleContents
import com.banuba.sdk.camera.Facing
import com.banuba.sdk.entity.RecordedVideoInfo
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.token.storage.license.EditorLicenseManager
import com.banuba.sdk.token.storage.provider.TokenProvider
import com.banuba.sdk.types.Data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayDeque

class CameraActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001

        private const val VIDEO_EXT = ".mp4"

        private const val RECORD_BTN_SCALE_FACTOR = 1.3f

        private const val SAMPLE_EFFECT = "AsaiLines"

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private var isFrontCamera = true
    private var captureMic = true
    private var speedRecording = 1f
    private var videosStack = ArrayDeque<Uri>()

    private lateinit var binding: ActivityCameraBinding

    private val tokenProvider: TokenProvider by inject(named("banubaTokenProvider"))

    private val cameraEventCallback = object : IEventCallback {
        override fun onCameraOpenError(p0: Throwable) {
        }

        override fun onCameraStatus(opened: Boolean) {
            runOnUiThread { binding.cameraSwitchButton.isClickable = opened }
        }

        override fun onScreenshotReady(bitmap: Bitmap) {
        }

        override fun onHQPhotoReady(bitmap: Bitmap) {
        }

        override fun onVideoRecordingFinished(info: RecordedVideoInfo) {
            videosStack.addLast(File(info.filePath).toUri())
            runOnUiThread {
                updateViews()
            }
        }

        override fun onVideoRecordingStatusChange(p0: Boolean) {
        }

        override fun onImageProcessed(bitmap: Bitmap) {
        }

        override fun onFrameRendered(p0: Data, p1: Int, p2: Int) {
        }
    }

    private val selectVideos = registerForActivityResult(GetMultipleContents()) {
        if (it.isNotEmpty()) {
            openEditor(it)
        }
    }

    private val timeBasedFileNameFormat = SimpleDateFormat("yyyy.MM.dd_HH.mm.ss", Locale.ENGLISH)

    private var banubaSdkManager: BanubaSdkManager? = null
    private var effectsHelper = BanubaEffectHelper()

    private var isBeautyApplied = false
    private var isMaskApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateViews()

        binding.applyBeautyButton.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.applyMaskButton.isChecked = false
                applyEffect("Beauty")
            } else {
                cancelEffect()
            }
        }

        binding.applyMaskButton.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.applyBeautyButton.isChecked = false
                applyEffect(SAMPLE_EFFECT)
            } else {
                cancelEffect()
            }
        }

        binding.openEditorButton.setOnClickListener {
            openEditor(videosStack.toList())
        }

        binding.removeLastVideoButton.setOnClickListener {
            videosStack.removeLastOrNull()
            updateViews()
        }

        binding.cameraSwitchButton.setOnClickListener {
            it.animate().rotationBy(it.rotationX + 180).setDuration(250).start()
            it.isClickable = false
            isFrontCamera = if (!isFrontCamera) {
                banubaSdkManager?.setCameraFacing(Facing.FRONT, true) ?: false
            } else {
                banubaSdkManager?.setCameraFacing(Facing.BACK, false)?.not() ?: false
            }
        }

        binding.cameraMicButton.setOnCheckedChangeListener { _, checked ->
            captureMic = checked
        }

        binding.cameraSpeedButton.apply {
            text = speedRecording.toString()
            setOnClickListener {
                speedRecording = if (speedRecording == 1f) .5f else 1f
                (it as TextView).text = speedRecording.toString()
            }
        }

        binding.galleryButton.setOnClickListener {
            selectVideos.launch("video/*")
        }

        setupShutterButton()
    }

    override fun onStart() {
        super.onStart()

        prepareFaceAR()
    }

    override fun onResume() {
        super.onResume()
        banubaSdkManager?.effectPlayer?.playbackPlay()
    }

    override fun onPause() {
        super.onPause()
        banubaSdkManager?.effectPlayer?.playbackPause()
    }

    override fun onStop() {
        super.onStop()
        banubaSdkManager?.closeCamera()
        banubaSdkManager?.releaseSurface()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyFaceAr()
        stopKoin()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (checkAllPermissionsGranted()) {
            banubaSdkManager?.openCamera()
        } else {
            Toast.makeText(applicationContext, "Please grant permission.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun applyEffect(name: String) {
        val manager = banubaSdkManager?.effectManager
        if (manager == null) {
            Log.w(
                "CameraActivity",
                "Cannot apply effect: Banuba Face AR Effect Player is not initialized"
            )
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val effect = effectsHelper.prepareEffect(assets, name)
                manager.loadAsync(effect.uri.toString())
            }
        }
    }

    private fun cancelEffect() {
        banubaSdkManager?.effectManager?.loadAsync("") ?: return
    }

    private fun prepareFaceAR() {
        if (banubaSdkManager == null) {
            initializeFaceAr()
        }
        banubaSdkManager?.attachSurface(binding.faceArSurfaceView)
        if (checkAllPermissionsGranted()) {
            banubaSdkManager?.openCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun initializeFaceAr() {
        BanubaSdkManager.deinitialize()
        BanubaSdkManager.initialize(applicationContext, getString(R.string.banuba_token))
        banubaSdkManager = BanubaSdkManager(applicationContext)
        banubaSdkManager?.setCallback(cameraEventCallback)
    }

    private fun destroyFaceAr() {
        banubaSdkManager?.closeCamera()
        banubaSdkManager?.setCallback(null)
        banubaSdkManager?.effectPlayer?.playbackStop()
        banubaSdkManager?.recycle()
        banubaSdkManager = null
        BanubaSdkManager.deinitialize()
    }

    private fun checkAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupShutterButton() {
        binding.recordButton.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view?.apply {
                        animate()
                            .scaleX(RECORD_BTN_SCALE_FACTOR)
                            .scaleY(RECORD_BTN_SCALE_FACTOR)
                            .setDuration(100)
                            .start()
                    }
                    startVideoRecord()
                }
                MotionEvent.ACTION_UP -> {
                    view?.apply {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    stopVideoRecord()
                }
            }
            true
        }
    }

    private fun startVideoRecord() {
        val fileNamePath = File(externalCacheDir, getTimeBasedFileName()).absolutePath
        banubaSdkManager?.startVideoRecording(
            fileNamePath,
            checkAllPermissionsGranted() && captureMic,
            null,
            speedRecording
        )
    }

    private fun stopVideoRecord() {
        banubaSdkManager?.stopVideoRecording()
    }

    private fun openEditor(videos: List<Uri>) {
        destroyFaceAr()
        initializeEditor()
        val intent = EditorActivity.createIntent(this, videos)
        startActivity(intent)
    }

    private fun initializeEditor() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            EditorLicenseManager.initialize(tokenProvider.getToken())
        }
    }

    private fun getTimeBasedFileName(): String {
        val name: String = timeBasedFileNameFormat.format(Date())
        return name + VIDEO_EXT
    }

    private fun updateViews() {
        binding.countOfVideos.text = if (videosStack.size > 0) {
            videosStack.size.toString()
        } else {
            ""
        }
        binding.openEditorButton.isVisible = videosStack.size > 0
        binding.galleryButton.isVisible = videosStack.size == 0
    }
}
