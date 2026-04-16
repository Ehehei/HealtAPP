package com.example.health.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = SamsungBg,
    surface = CardBg,
    onBackground = OnSurface,
    onSurface = OnSurface,
    secondary = AccentMinutes,
    tertiary = AccentSteps,
)

@Composable
fun HealthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
