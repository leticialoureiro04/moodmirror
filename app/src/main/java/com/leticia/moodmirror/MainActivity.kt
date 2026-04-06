package com.leticia.moodmirror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leticia.moodmirror.sensors.SensorManagerHelper
import com.leticia.moodmirror.ui.screens.HistoryScreen
import com.leticia.moodmirror.ui.screens.IntroScreen
import com.leticia.moodmirror.ui.screens.MainScreen
import com.leticia.moodmirror.ui.theme.MoodMirrorTheme
import com.leticia.moodmirror.viewmodel.MoodViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoodMirrorTheme {
                val moodViewModel: MoodViewModel = viewModel()
                val uiState by moodViewModel.uiState.collectAsStateWithLifecycle()
                val historyRecords by moodViewModel.historyRecords.collectAsStateWithLifecycle()
                var started by rememberSaveable { mutableStateOf(false) }
                var showHistory by rememberSaveable { mutableStateOf(false) }

                DisposableEffect(Unit) {
                    // Sensores ativos apenas enquanto a UI desta Activity estiver composable.
                    val sensorHelper = SensorManagerHelper(
                        context = this@MainActivity,
                        onLightSensorAvailable = moodViewModel::onLightSensorAvailable,
                        onLightChanged = moodViewModel::onLightChanged,
                        onMotionChanged = moodViewModel::onMotionChanged
                    )
                    sensorHelper.register()

                    onDispose {
                        sensorHelper.unregister()
                    }
                }

                // Navegacao simples por estado para evitar dependencias extras.
                if (!started) {
                    IntroScreen(onStartClick = { started = true })
                } else if (showHistory) {
                    HistoryScreen(
                        records = historyRecords,
                        onBackClick = { showHistory = false }
                    )
                } else {
                    MainScreen(
                        uiState = uiState,
                        onFaceData = moodViewModel::onFaceData,
                        onSaveClick = moodViewModel::saveCurrentRecord,
                        onOpenHistoryClick = { showHistory = true }
                    )
                }
            }
        }
    }
}
