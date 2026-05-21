package com.example.health.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = SurfaceCard,
    primaryContainer = BrandGreenSoftBg,
    onPrimaryContainer = BrandGreenText,

    secondary = AccentBlue,
    onSecondary = SurfaceCard,
    secondaryContainer = BlueSoftBg,
    onSecondaryContainer = BlueSoftText,

    tertiary = AccentAmber,
    onTertiary = SurfaceCard,
    tertiaryContainer = AmberSoftBg,
    onTertiaryContainer = AmberSoftText,

    error = SosRed,
    onError = SurfaceCard,
    errorContainer = SosBannerBg,
    onErrorContainer = SosTextDeep,

    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,

    outline = BorderHairline,
    outlineVariant = BorderHairline,
)

@Composable
fun HealthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
