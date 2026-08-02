package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTheme {

    @Composable
    fun cardColors(): CardColors {
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x33FFFFFF) else Color(0xFFFFFFFF),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    @Composable
    fun borderStroke(width: Dp = 1.dp): BorderStroke {
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return BorderStroke(
            width = width,
            color = if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)
        )
    }

    @Composable
    fun dialogColors(): CardColors {
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xF2121212) else Color(0xF2FFFFFF),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    val cornerRadius = RoundedCornerShape(24.dp)
    val buttonCornerRadius = RoundedCornerShape(16.dp)
}
