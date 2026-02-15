package com.fahh.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(@ApplicationContext private val context: Context) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
        onCaptureReady: (VideoCapture<Recorder>) -> Unit,
        onError: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    this.cameraProvider = cameraProvider

                    previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val qualitySelector = QualitySelector.fromOrderedList(
                        listOf(Quality.FHD, Quality.HD, Quality.SD),
                        FallbackStrategy.lowerQualityThan(Quality.FHD)
                    )
                    val recorder = Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build()
                    videoCapture = VideoCapture.withOutput(recorder)

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )

                    videoCapture?.let { onCaptureReady(it) }
                } catch (securityException: SecurityException) {
                    onError("Camera permission is required.")
                } catch (exception: Exception) {
                    onError("Failed to start camera preview.")
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun releaseCamera() {
        cameraProvider?.unbindAll()
        videoCapture = null
    }
}
