package com.victor.loclarm2.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberpunkYellow,
    onPrimary = CyberpunkBlack,
    primaryContainer = CyberpunkYellow.copy(alpha = 0.3f),
    onPrimaryContainer = CyberpunkYellow,

    secondary = CyberpunkBlue,
    onSecondary = CyberpunkBlack,
    secondaryContainer = CyberpunkBlue.copy(alpha = 0.3f),
    onSecondaryContainer = CyberpunkBlue,

    tertiary = CyberpunkPink,
    onTertiary = CyberpunkBlack,
    tertiaryContainer = CyberpunkPink.copy(alpha = 0.3f),
    onTertiaryContainer = CyberpunkPink,

    background = CyberpunkDark,
    onBackground = CyberpunkWhite,

    surface = CyberpunkLightGray,
    onSurface = CyberpunkWhite,
    surfaceVariant = CyberpunkDark,
    onSurfaceVariant = CyberpunkWhite.copy(alpha = 0.7f),

    outline = CyberpunkWhite.copy(alpha = 0.5f),
    outlineVariant = CyberpunkWhite.copy(alpha = 0.3f),

    error = CyberpunkPink,
    onError = CyberpunkBlack
)

private val LightColorScheme = lightColorScheme(
    primary = CyberpunkYellowDark,
    onPrimary = CyberpunkWhite,
    primaryContainer = CyberpunkYellowLight,
    onPrimaryContainer = CyberpunkYellowDark,

    secondary = CyberpunkBlueDark,
    onSecondary = CyberpunkWhite,
    secondaryContainer = CyberpunkBlueLight,
    onSecondaryContainer = CyberpunkBlueDark,

    tertiary = CyberpunkPinkDark,
    onTertiary = CyberpunkWhite,
    tertiaryContainer = CyberpunkPinkLight,
    onTertiaryContainer = CyberpunkPinkDark,

    background = CyberpunkLightBackground,
    onBackground = CyberpunkLightOnSurface,

    surface = CyberpunkLightSurface,
    onSurface = CyberpunkLightOnSurface,
    surfaceVariant = CyberpunkLightSecondary,
    onSurfaceVariant = CyberpunkLightOnSurface.copy(alpha = 0.7f),

    outline = CyberpunkLightOnSurface.copy(alpha = 0.5f),
    outlineVariant = CyberpunkLightOnSurface.copy(alpha = 0.3f),

    error = CyberpunkPinkDark,
    onError = CyberpunkWhite
)

@Composable
fun Loclarm2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}