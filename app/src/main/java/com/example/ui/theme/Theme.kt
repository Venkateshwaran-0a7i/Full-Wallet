package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getAccentColor(accentKey: String): Color {
    return when (accentKey.lowercase()) {
        "emerald" -> Color(0xFF059669)
        "blue" -> Color(0xFF2563EB)
        "purple" -> Color(0xFF7C3AED)
        "amber" -> Color(0xFFD97706)
        "rose" -> Color(0xFFE11D48)
        "teal" -> Color(0xFF0D9488)
        "coral" -> Color(0xFFEA580C)
        else -> Color(0xFF0F9D58) // Default Green
    }
}

@Composable
fun MyWalletIsFullTheme(
    themePreference: String = "dark",
    accentColorPreference: String = "green",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themePreference.lowercase()) {
        "dark" -> true
        "light" -> false
        "system" -> isSystemDark
        else -> true
    }

    val primaryColor = getAccentColor(accentColorPreference)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = primaryColor.copy(alpha = 0.85f),
            onSecondary = Color.White,
            tertiary = Color(0xFF222222),
            onTertiary = Color.White,
            background = Color(0xFF0A0A0A),
            onBackground = Color.White,
            surface = Color(0xFF141414),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1F1F1F),
            onSurfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFF333333)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = primaryColor,
            onSecondary = Color.White,
            tertiary = primaryColor.copy(alpha = 0.12f),
            onTertiary = primaryColor,
            background = Color(0xFFF8FAF8),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF334155),
            outline = Color(0xFFCBD5E1)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
