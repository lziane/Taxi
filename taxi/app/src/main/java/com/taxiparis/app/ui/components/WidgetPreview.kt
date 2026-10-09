package com.taxiparis.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxiparis.app.domain.model.TaxiState
import com.taxiparis.app.data.mapper.TrafficMapper
import com.taxiparis.app.ui.util.formatMinutesWidget
import java.util.Locale

@Composable
fun WidgetPreview(state: TaxiState) {
    val isDark = when(state.appTheme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    
    val enabledLines = state.enabledLines
    val isMaxTrans = state.isWidgetTransparent && state.isMaxTransparency
    
    val widgetBg = when {
        isMaxTrans -> Color.Transparent
        state.isWidgetTransparent -> if (isDark) Color(0xB31C1C1E) else Color(0xCCFFFFFF)
        else -> if (isDark) Color(0xFF1C1C1E) else Color.White
    }
    
    val textColorHex = state.widgetTextColor
    val textCol = if (textColorHex != 0) Color(textColorHex) else if (isDark) Color.White else Color(0xFF1C1C1E)
    val blueCol = if (isDark) Color(0xFF38BDF8) else Color(0xFF007AFF)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreviewAirportWidget("CDG", 35, "28 min", 23.3, 0, 1, widgetBg, textCol, blueCol, state.widgetFontSize, isMaxTrans, Modifier.weight(1f))
            PreviewAirportWidget("ORL", 15, "45 min", 18.2, 1, 2, widgetBg, textCol, blueCol, state.widgetFontSize, isMaxTrans, Modifier.weight(1f))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = widgetBg,
            border = if (isMaxTrans) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Métro", color = textCol, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (state.isMetroRefreshing) {
                        Text("...", color = textCol, fontSize = 12.sp)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                val displayLines = enabledLines.take(18).chunked(6)
                displayLines.forEach { rowLines ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowLines.forEach { lineId ->
                            val lineName = TrafficMapper.LINES_MAP[lineId] ?: lineId
                            Box(
                                modifier = Modifier.size(28.dp).background(Color(0xFF34C759), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lineName.replace("Métro ", "").replace("RER ", ""),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        repeat(6 - rowLines.size) {
                            Spacer(Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewAirportWidget(
    name: String, 
    minutes: Int, 
    travelTime: String, 
    distance: Double, 
    trafficStatus: Int,
    trend: Int,
    bgColor: Color,
    textColor: Color,
    blueColor: Color,
    fontSize: Int,
    isMaxTrans: Boolean,
    modifier: Modifier = Modifier
) {
    val trafficColor = when(trafficStatus) {
        1 -> Color(0xFFFF9500)
        2 -> Color(0xFFFF3B30)
        else -> Color(0xFF34C759)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = if (isMaxTrans) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (trend != 0) {
                    Icon(
                        imageVector = if (trend == 1) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp).size(14.dp),
                        tint = if (trend == 1) Color(0xFFFF3B30) else Color(0xFF34C759)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(travelTime, color = trafficColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = formatMinutesWidget(minutes),
                    style = TextStyle(color = blueColor, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    color = blueColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", distance)} km",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Il y a 1min",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
