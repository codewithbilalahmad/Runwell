package com.muhammad.core.presentation.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = RunwellGreen,
    background = RunwellBlack,
    surface = RunwellDarkGray,
    secondary = RunwellWhite,
    tertiary = RunwellWhite,
    primaryContainer = RunwellGreen30,
    onPrimary = RunwellBlack,
    onBackground = RunwellWhite,
    onSurface = RunwellWhite,
    onSurfaceVariant = RunwellGray,
    error = RunwellDarkRed,
    errorContainer = RunwellDarkRed5
)

@Composable
fun RunwellTheme(content : @Composable () -> Unit) {
    val colorScheme=  DarkColorScheme
    val view = LocalView.current
    if(!view.isInEditMode){
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}