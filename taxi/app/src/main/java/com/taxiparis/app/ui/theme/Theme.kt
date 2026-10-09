package com.taxiparis.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TaxiBlue = Color(0xFF007AFF)
private val TaxiDark = Color(0xFF1C1C1E)
private val TaxiLight = Color(0xFFF2F2F7)

private val DarkColorScheme = darkColorScheme(
    primary = TaxiBlue,
    onPrimary = Color.White,
    secondary = Color.Gray,
    surface = TaxiDark,
    onSurface = Color.White,
    background = Color.Black,
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TaxiBlue,
    onPrimary = Color.White,
    secondary = Color.Gray,
    surface = Color.White,
    onSurface = Color.Black,
    background = TaxiLight,
    onBackground = Color.Black
)

@Composable
fun TaxiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
