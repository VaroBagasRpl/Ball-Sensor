package com.example.balanceball.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK,
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
)

/**
 * Custom Material 3 Theme dengan dukungan hysteresis cahaya (lux):
 * - Lux < 20 -> Dark Theme
 * - Lux > 60 -> Light Theme
 * Transisi warna dianimasikan secara mulus via animateColorAsState.
 */
@Composable
fun SensorPlaygroundTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    currentLux: Float = 200f,
    isDarkState: Boolean = false,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> {
            if (currentLux < 20f) {
                true
            } else if (currentLux > 60f) {
                false
            } else {
                isDarkState
            }
        }
    }

    val targetScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    val animatedBackground by animateColorAsState(targetScheme.background, animationSpec = tween(500), label = "bg")
    val animatedSurface by animateColorAsState(targetScheme.surface, animationSpec = tween(500), label = "surface")
    val animatedPrimary by animateColorAsState(targetScheme.primary, animationSpec = tween(500), label = "primary")
    val animatedOnBackground by animateColorAsState(targetScheme.onBackground, animationSpec = tween(500), label = "onBg")
    val animatedOnSurface by animateColorAsState(targetScheme.onSurface, animationSpec = tween(500), label = "onSurface")

    val animatedColorScheme = targetScheme.copy(
        background = animatedBackground,
        surface = animatedSurface,
        primary = animatedPrimary,
        onBackground = animatedOnBackground,
        onSurface = animatedOnSurface,
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content,
    )
}
