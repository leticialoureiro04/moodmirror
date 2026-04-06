package com.leticia.moodmirror.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val onFacesDetected: (List<Face>) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: FaceDetector by lazy {
        // Opcoes leves para analise em tempo real numa demonstracao.
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build()
        FaceDetection.getClient(options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                onFacesDetected(faces)
            }
            .addOnFailureListener {
                // Falha de frame nao deve quebrar o fluxo da analise em tempo real.
                onFacesDetected(emptyList())
            }
            .addOnCompleteListener {
                // Libertar sempre o frame para evitar bloquear o pipeline do CameraX.
                imageProxy.close()
            }
    }

    fun stop() {
        detector.close()
    }
}
