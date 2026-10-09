package com.taxiparis.app.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

/**
 * Returns the official color associated with a Paris Metro or RER line.
 */
fun getLineColor(name: String): Color = when {
    name.contains("1") && !name.contains("b") -> Color(0xFFFFBE00)
    name.contains("2") -> Color(0xFF0055C8)
    name.contains("3") && !name.contains("b") -> Color(0xFF6E6E00)
    name.contains("3b") -> Color(0xFF92DBE6)
    name.contains("4") -> Color(0xFFCF009E)
    name.contains("5") -> Color(0xFFFF7E2E)
    name.contains("6") -> Color(0xFF6ECA97)
    name.contains("7") && !name.contains("b") -> Color(0xFFFA87A0)
    name.contains("7b") -> Color(0xFF6ECA97)
    name.contains("8") -> Color(0xFFE19BDF)
    name.contains("9") -> Color(0xFFB6BD00)
    name.contains("10") -> Color(0xFFC9910D)
    name.contains("11") -> Color(0xFF704B1C)
    name.contains("12") -> Color(0xFF007852)
    name.contains("13") -> Color(0xFF00B1E6)
    name.contains("14") -> Color(0xFF62259D)
    name.contains("RER A") -> Color(0xFFE3051C)
    name.contains("RER B") -> Color(0xFF5291CE)
    name.contains("RER C") -> Color(0xFFFFCC00)
    name.contains("RER D") -> Color(0xFF00814F)
    name.contains("RER E") -> Color(0xFFBD76A1)
    else -> Color.Gray
}

/**
 * Formats a duration in minutes into a human-readable string (e.g., "1h15" or "45m").
 */
fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "--"
    return if (minutes >= 60) "${minutes / 60}h${(minutes % 60).toString().padStart(2, '0')}" else "${minutes}m"
}

/**
 * Formats a duration in minutes for the widget display (e.g., "1:15" or "45").
 */
fun formatMinutesWidget(minutes: Int): String {
    if (minutes <= 0) return "--"
    return if (minutes >= 60) "${minutes / 60}:${(minutes % 60).toString().padStart(2, '0')}" else "${minutes}"
}

/**
 * Formats a Unix timestamp into a relative time string.
 */
fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Jamais"
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / (1000 * 60)
    
    return when {
        mins < 1 -> "À l'instant"
        mins < 60 -> "Il y a ${mins}min"
        else -> {
            val sdf = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            "Le ${sdf.format(java.util.Date(timestamp))}"
        }
    }
}

/**
 * Maps a weather condition code to its corresponding icon.
 */
fun getWeatherIcon(code: Int): ImageVector = when (code) {
    0 -> Icons.Rounded.WbSunny
    1, 2, 3 -> Icons.Rounded.CloudQueue
    45, 48 -> Icons.Rounded.Cloud
    else -> Icons.Rounded.WbCloudy
}
