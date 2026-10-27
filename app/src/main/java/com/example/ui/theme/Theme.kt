package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SystemCyan,
    onPrimary = Color.Black,
    primaryContainer = MonarchPurpleDark,
    onPrimaryContainer = SystemCyan,
    secondary = MonarchPurple,
    onSecondary = Color.White,
    secondaryContainer = SystemSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = CrimsonPenaltyRed,
    onTertiary = Color.White,
    background = SystemAmoledBg,
    onBackground = TextPrimary,
    surface = SystemSurface,
    onSurface = TextPrimary,
    surfaceVariant = SystemSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SystemSurfaceBorder,
    outlineVariant = SystemCyanGlow
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
