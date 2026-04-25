package io.lumina.luminaux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HyphenColorScheme = darkColorScheme(
    primary          = HyphenCyan,
    secondary        = HyphenIndigo,
    tertiary         = Pink80,
    background       = HyphenBg,
    surface          = HyphenSurface,
    onPrimary        = HyphenBg,
    onSecondary      = HyphenOnSurface,
    onBackground     = HyphenOnSurface,
    onSurface        = HyphenOnSurface,
    surfaceVariant   = HyphenSurface2,
    onSurfaceVariant = HyphenOnSurfaceVariant
)

@Composable
fun HyphenUXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HyphenColorScheme,
        typography   = Typography,
        content      = content
    )
}

// Backward-compat alias used by MainActivity
@Composable
fun LuminaUXTheme(content: @Composable () -> Unit) = HyphenUXTheme(content)
