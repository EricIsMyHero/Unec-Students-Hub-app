package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val LocalUnecPalette = staticCompositionLocalOf { UnecThemes.first() }

val UnecShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun UnecTheme(
    themeKey: String = "unec",
    content: @Composable () -> Unit
) {
    val palette = paletteForKey(themeKey)
    val onAccent = if (!palette.isLight && palette.key == "neon") Color(0xFF03150B)
    else if (!palette.isLight && palette.key == "midnight") Color(0xFF0A0A0A)
    else Color.White

    val colorScheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = onAccent,
            primaryContainer = palette.accent.copy(alpha = 0.14f),
            onPrimaryContainer = palette.accent,
            secondary = palette.accent2,
            onSecondary = Color.White,
            secondaryContainer = palette.surfaceInner,
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.cyan,
            background = palette.bg,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceInner,
            onSurfaceVariant = palette.textMuted,
            outline = palette.border,
            outlineVariant = palette.border.copy(alpha = 0.6f),
            error = palette.danger,
            onError = Color.White,
        )
    } else {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = onAccent,
            primaryContainer = palette.accent.copy(alpha = 0.18f),
            onPrimaryContainer = palette.accent2,
            secondary = palette.accent2,
            onSecondary = Color(0xFF030B19),
            secondaryContainer = palette.surfaceInner,
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.cyan,
            background = palette.bg,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceInner,
            onSurfaceVariant = palette.textMuted,
            outline = palette.border,
            outlineVariant = palette.border.copy(alpha = 0.6f),
            error = palette.danger,
            onError = Color.White,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = palette.isLight
                controller.isAppearanceLightNavigationBars = palette.isLight
            }
        }
    }

    CompositionLocalProvider(LocalUnecPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = UnecTypography,
            shapes = UnecShapes,
            content = content
        )
    }
}
