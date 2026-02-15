package com.fahh.viewmodel

import android.app.Application
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.fahh.camera.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application,
    private val cameraManager: CameraManager
) : AndroidViewModel(application) {

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector = _cameraSelector.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingTimer = MutableStateFlow("00:00")
    val recordingTimer = _recordingTimer.asStateFlow()

    private var currentRecording: androidx.camera.video.Recording? = null
    private var timerJob: Job? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCaptureReady: (VideoCapture<Recorder>) -> Unit,
        onError: (String) -> Unit
    ) {
        cameraManager.startCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            cameraSelector = _cameraSelector.value,
            onCaptureReady = onCaptureReady,
            onError = onError
        )
    }

    fun toggleCamera() {
        _cameraSelector.value = if (_cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun startRecording(
        videoCapture: VideoCapture<Recorder>,
        onVideoSaved: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isRecording.value) return

        val name = "fahh_" +
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())

        val outputFile = File(getOutputDirectory(), "$name.mp4")
        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        try {
            _isRecording.value = true
            startTimer()

            val pendingRecording = videoCapture.output
                .prepareRecording(getApplication(), outputOptions)

            val hasAudioPermission = ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            currentRecording = (if (hasAudioPermission) pendingRecording.withAudioEnabled() else pendingRecording)
                .start(ContextCompat.getMainExecutor(getApplication())) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            _isRecording.value = true
                        }

                        is VideoRecordEvent.Finalize -> {
                            currentRecording = null
                            stopTimer()
                            _isRecording.value = false

                            if (!recordEvent.hasError()) {
                                val isFileValid = outputFile.exists() && outputFile.length() > 4_096
                                if (!isFileValid) {
                                    onError("Recording failed. Output file was not created correctly.")
                                    return@start
                                }

                                val copiedToGallery = copyRecordingToGallery(outputFile)
                                if (!copiedToGallery) {
                                    MediaScannerConnection.scanFile(
                                        getApplication(),
                                        arrayOf(outputFile.absolutePath),
                                        arrayOf("video/mp4"),
                                        null
                                    )
                                }
                                onVideoSaved(outputFile)
                            } else {
                                onError("Recording failed (code ${recordEvent.error}). Please try again.")
                            }
                        }
                    }
                }
        } catch (_: SecurityException) {
            currentRecording = null
            stopTimer()
            _isRecording.value = false
            onError("Camera and microphone permissions are required.")
        } catch (_: Exception) {
            currentRecording = null
            stopTimer()
            _isRecording.value = false
            onError("Unable to start recording.")
        }
    }

    fun stopRecording() {
        try {
            currentRecording?.stop()
        } finally {
            currentRecording = null
            stopTimer()
            _isRecording.value = false
        }
    }

    private fun getOutputDirectory(): File {
        val app = getApplication<Application>()
        val moviesDir = app.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?.let { File(it, "fahh").apply { mkdirs() } }

        return if (moviesDir != null && moviesDir.exists()) {
            moviesDir
        } else {
            app.filesDir
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _recordingTimer.value = "00:00"
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                val minutesPart = seconds / 60
                val secondsPart = seconds % 60
                _recordingTimer.value = String.format(Locale.US, "%02d:%02d", minutesPart, secondsPart)
                delay(1000)
                seconds += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _recordingTimer.value = "00:00"
    }

    private fun copyRecordingToGallery(sourceFile: File): Boolean {
        if (!sourceFile.exists()) return false

        val app = getApplication<Application>()
        val resolver = app.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/fahh")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val targetUri = resolver.insert(collection, values) ?: return false

        return try {
            resolver.openOutputStream(targetUri)?.use { out ->
                sourceFile.inputStream().use { input -> input.copyTo(out) }
            } ?: throw IOException("Unable to open MediaStore output stream.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val publishValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                resolver.update(targetUri, publishValues, null, null)
            }

            true
        } catch (_: Exception) {
            resolver.delete(targetUri, null, null)
            false
        }
    }
}
