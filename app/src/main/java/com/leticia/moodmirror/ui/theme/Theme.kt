package com.leticia.moodmirror.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MoodColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    surface = SurfaceLight
)

@Composable
fun MoodMirrorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MoodColorScheme,
        typography = AppTypography,
        content = content
    )
}
