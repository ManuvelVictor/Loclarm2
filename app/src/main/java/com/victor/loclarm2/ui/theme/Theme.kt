package com.victor.loclarm2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberpunkYellow,
    onPrimary = CyberpunkBlack,
    secondary = CyberpunkBlue,
    onSecondary = CyberpunkBlack,
    tertiary = CyberpunkPink,
    onTertiary = CyberpunkBlack,
    background = CyberpunkDark,
    onBackground = CyberpunkWhite,
    surface = CyberpunkDark,
    onSurface = CyberpunkWhite
)

private val LightColorScheme = lightColorScheme(
    primary = CyberpunkYellow,
    onPrimary = CyberpunkBlack,
    secondary = CyberpunkBlue,
    onSecondary = CyberpunkBlack,
    tertiary = CyberpunkPink,
    onTertiary = CyberpunkBlack,
    background = CyberpunkLightGray,
    onBackground = CyberpunkWhite,
    surface = CyberpunkLightGray,
    onSurface = CyberpunkWhite
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