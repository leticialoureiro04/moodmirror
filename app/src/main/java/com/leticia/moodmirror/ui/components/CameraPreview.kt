package com.leticia.moodmirror.ui.components

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.face.Face
import com.leticia.moodmirror.camera.FaceAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    useFrontCamera: Boolean = true,
    onFaceData: (List<Face>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val faceAnalyzer = remember { FaceAnalyzer(onFaceData) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, useFrontCamera) {
        // A camera e reconfigurada automaticamente ao trocar frontal/traseira.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    // Processa apenas o frame mais recente para manter fluidez.
                    it.setAnalyzer(cameraExecutor, faceAnalyzer)
                }

            try {
                val preferredSelector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                val fallbackSelector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                val finalSelector = when {
                    cameraProvider.hasCamera(preferredSelector) -> preferredSelector
                    cameraProvider.hasCamera(fallbackSelector) -> fallbackSelector
                    else -> throw IllegalStateException("No camera available")
                }

                // Usa em paralelo Preview + ImageAnalysis (requisito CameraX).
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    finalSelector,
                    preview,
                    analysis
                )
            } catch (_: Exception) {
                onFaceData(emptyList())
            }
        }

        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            kotlin.runCatching {
                cameraProviderFuture.get().unbindAll()
            }
            faceAnalyzer.stop()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}
