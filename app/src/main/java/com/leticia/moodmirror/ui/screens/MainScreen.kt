package com.leticia.moodmirror.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.Face
import com.leticia.moodmirror.ui.components.CameraPreview
import com.leticia.moodmirror.viewmodel.AnalysisQuality
import com.leticia.moodmirror.viewmodel.MoodUiState

@Composable
fun MainScreen(
    uiState: MoodUiState,
    onFaceData: (List<Face>) -> Unit,
    onSaveClick: () -> Unit,
    onOpenHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    var useFrontCamera by remember { mutableStateOf(true) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        // Solicita permissao da camera apenas uma vez ao entrar neste ecrã.
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3F6FA), Color(0xFFE4ECF7))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "MoodMirror - Análise em Tempo Real",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        QualityCard(uiState = uiState)

        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Black, shape = RoundedCornerShape(20.dp))
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    useFrontCamera = useFrontCamera,
                    onFaceData = onFaceData
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { useFrontCamera = !useFrontCamera }) {
                    Text(if (useFrontCamera) "Usar câmara traseira" else "Usar câmara frontal")
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permissão da câmara necessária para continuar.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Conceder permissão")
                    }
                }
            }
        }

        FeedbackCard(title = "Face", message = uiState.faceMessage)
        FeedbackCard(title = "Emoção", message = uiState.emotionMessage)
        FeedbackCard(
            title = "Luz",
            message = if (uiState.lightSensorAvailable) {
                // Em emulador, 0 lux e comum por ausencia de sensor fisico.
                if (uiState.lightLevelLux <= 0f) {
                    "Sem leitura útil do sensor de luz (comum em emulador)."
                } else {
                    "${uiState.lightMessage} (${uiState.lightLevelLux.toInt()} lux)"
                }
            } else {
                uiState.lightMessage
            },
            tone = when {
                !uiState.lightSensorAvailable || uiState.lightLevelLux <= 0f -> FeedbackTone.WARNING
                uiState.lightLevelLux < 60f -> FeedbackTone.WARNING
                else -> FeedbackTone.GOOD
            }
        )
        FeedbackCard(
            title = "Movimento",
            message = uiState.motionMessage,
            tone = if (uiState.isMoving) FeedbackTone.WARNING else FeedbackTone.GOOD
        )
        FeedbackCard(
            title = "Feedback Final",
            message = uiState.overallMessage,
            tone = when (uiState.quality) {
                AnalysisQuality.BOA -> FeedbackTone.GOOD
                AnalysisQuality.MEDIA -> FeedbackTone.WARNING
                AnalysisQuality.FRACA -> FeedbackTone.NEUTRAL
            }
        )

        Text(
            text = "Nota: análise académica, sem valor clínico.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF546E7A),
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permite guardar rapidamente o estado atual e abrir o historico.
            Button(
                modifier = Modifier.weight(1f),
                onClick = onSaveClick
            ) {
                Text("Guardar registo")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onOpenHistoryClick
            ) {
                Text("Ver histórico")
            }
        }

        uiState.saveStatusMessage?.let { status ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    title: String,
    message: String,
    tone: FeedbackTone = FeedbackTone.NEUTRAL
) {
    val color = when (tone) {
        FeedbackTone.GOOD -> Color(0xFFE8F5E9)
        FeedbackTone.WARNING -> Color(0xFFFFF8E1)
        FeedbackTone.NEUTRAL -> Color(0xFFF3EDF7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QualityCard(uiState: MoodUiState) {
    val bg = when (uiState.quality) {
        AnalysisQuality.BOA -> Color(0xFFC8E6C9)
        AnalysisQuality.MEDIA -> Color(0xFFFFECB3)
        AnalysisQuality.FRACA -> Color(0xFFFFCDD2)
    }
    val symbol = when (uiState.quality) {
        AnalysisQuality.BOA -> "OK"
        AnalysisQuality.MEDIA -> "!"
        AnalysisQuality.FRACA -> "X"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.qualityMessage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private enum class FeedbackTone {
    GOOD,
    WARNING,
    NEUTRAL
}
