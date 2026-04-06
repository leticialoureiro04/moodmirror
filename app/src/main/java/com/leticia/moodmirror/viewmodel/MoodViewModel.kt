package com.leticia.moodmirror.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import com.leticia.moodmirror.data.MoodRepository
import com.leticia.moodmirror.data.local.MoodDatabase
import com.leticia.moodmirror.data.local.MoodRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AnalysisQuality {
    BOA,
    MEDIA,
    FRACA
}

data class MoodUiState(
    val lightSensorAvailable: Boolean = true,
    val lightLevelLux: Float = 0f,
    val lightMessage: String = "A ler luminosidade...",
    val isMoving: Boolean = false,
    val motionMessage: String = "A analisar movimento...",
    val faceDetected: Boolean = false,
    val faceMessage: String = "A procurar rosto...",
    val emotionMessage: String = "A analisar emoção...",
    val currentEmotionLabel: String = "Indefinida",
    val fatigueLikely: Boolean = false,
    val quality: AnalysisQuality = AnalysisQuality.FRACA,
    val qualityMessage: String = "Qualidade da análise: Fraca",
    val overallMessage: String = "A aguardar dados do ambiente e câmara.",
    val saveStatusMessage: String? = null
)

class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MoodRepository(
        MoodDatabase.getInstance(application).moodRecordDao()
    )

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    val historyRecords: StateFlow<List<MoodRecordEntity>> = repository.observeRecords().stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onLightSensorAvailable(available: Boolean) {
        _uiState.update { current ->
            current.copy(
                lightSensorAvailable = available,
                lightMessage = if (available) {
                    current.lightMessage
                } else {
                    "Sensor de luz indisponível (fallback ativo)."
                }
            )
        }
        recalculateOverallMessage()
    }

    fun onLightChanged(lux: Float) {
        val message = when {
            lux < 20f -> "Ambiente com pouca luz"
            lux < 150f -> "Ambiente moderadamente iluminado"
            else -> "Ambiente bem iluminado"
        }

        _uiState.update { current ->
            current.copy(
                lightLevelLux = lux,
                lightMessage = if (current.lightSensorAvailable) message else current.lightMessage
            )
        }
        recalculateOverallMessage()
    }

    fun onMotionChanged(isMoving: Boolean) {
        _uiState.update {
            it.copy(
                isMoving = isMoving,
                motionMessage = if (isMoving) "Dispositivo em movimento" else "Dispositivo estável"
            )
        }
        recalculateOverallMessage()
    }

    fun onFaceData(faces: List<Face>) {
        if (faces.isEmpty()) {
            _uiState.update {
                it.copy(
                    faceDetected = false,
                    fatigueLikely = false,
                    faceMessage = "Nenhum rosto detetado",
                    emotionMessage = "Emoção indisponível (sem rosto).",
                    currentEmotionLabel = "Indefinida"
                )
            }
            recalculateOverallMessage()
            return
        }

        val mainFace = faces.first()
        val leftEye = mainFace.leftEyeOpenProbability ?: 1f
        val rightEye = mainFace.rightEyeOpenProbability ?: 1f
        val smile = mainFace.smilingProbability ?: 0.5f

        val fatigueLikely = leftEye < 0.35f && rightEye < 0.35f
        val emotion = when {
            fatigueLikely -> "Cansada/o"
            smile > 0.7f -> "Feliz"
            smile < 0.25f && leftEye > 0.5f && rightEye > 0.5f -> "Triste ou baixa energia"
            else -> "Neutra/o"
        }

        _uiState.update {
            it.copy(
                faceDetected = true,
                fatigueLikely = fatigueLikely,
                faceMessage = if (fatigueLikely) "Possível fadiga" else "Rosto detetado com sucesso",
                emotionMessage = "Emoção estimada: $emotion",
                currentEmotionLabel = emotion
            )
        }
        recalculateOverallMessage()
    }

    fun saveCurrentRecord() {
        val snapshot = _uiState.value
        viewModelScope.launch {
            repository.insert(
                MoodRecordEntity(
                    timestamp = System.currentTimeMillis(),
                    emotion = snapshot.currentEmotionLabel,
                    faceDetected = snapshot.faceDetected,
                    isMoving = snapshot.isMoving,
                    lightLux = snapshot.lightLevelLux,
                    overallMessage = snapshot.overallMessage
                )
            )

            _uiState.update { it.copy(saveStatusMessage = "Registo guardado no histórico.") }
        }
    }

    fun clearSaveStatusMessage() {
        _uiState.update { it.copy(saveStatusMessage = null) }
    }

    private fun recalculateOverallMessage() {
        _uiState.update { current ->
            val enoughLight = !current.lightSensorAvailable || current.lightLevelLux >= 60f
            val stable = !current.isMoving
            val faceReady = current.faceDetected
            val fatigue = current.fatigueLikely

            val score = listOf(faceReady, enoughLight, stable, !fatigue).count { it }
            val quality = when {
                score >= 4 -> AnalysisQuality.BOA
                score >= 2 -> AnalysisQuality.MEDIA
                else -> AnalysisQuality.FRACA
            }
            val qualityMessage = when (quality) {
                AnalysisQuality.BOA -> "Qualidade da análise: Boa"
                AnalysisQuality.MEDIA -> "Qualidade da análise: Média"
                AnalysisQuality.FRACA -> "Qualidade da análise: Fraca"
            }

            val overall = when {
                !faceReady -> "Posicione o rosto em frente à câmara."
                fatigue -> "Possível fadiga. Faça uma pausa breve."
                enoughLight && stable && faceReady -> "Condições adequadas para concentração."
                !enoughLight -> "Aumente a iluminação para melhor conforto."
                !stable -> "Segure o dispositivo com mais estabilidade."
                else -> "Ajuste o ambiente para uma melhor experiência."
            }

            current.copy(
                quality = quality,
                qualityMessage = qualityMessage,
                overallMessage = overall
            )
        }
    }
}
