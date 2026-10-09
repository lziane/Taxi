package com.taxiparis.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import com.taxiparis.app.R
import java.util.Locale
import com.taxiparis.app.common.LocationTracker
import com.taxiparis.app.data.local.TaxiPrefs
import com.taxiparis.app.domain.model.TaxiState
import com.taxiparis.app.domain.model.LineStatus
import com.taxiparis.app.data.mapper.TrafficMapper
import kotlinx.serialization.json.Json
import com.taxiparis.app.ui.util.formatMinutesWidget
import com.taxiparis.app.worker.UpdateWorker
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

val destKey = ActionParameters.Key<String>("destination")

abstract class BaseAirportWidget(val type: String) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = TaxiPrefs(context)
        provideContent {
            val state by prefs.taxiState.collectAsState(initial = TaxiState(0, 0, 0L, false))
            AirportWidgetContent(state)
        }
    }

    @Composable
    private fun AirportWidgetContent(state: TaxiState) {
        val context = LocalContext.current
        val name = if (type == "cdg") "CDG" else "ORL"
        val minutes = if (type == "cdg") state.cdgMinutes else state.oryMinutes
        val trend = if (type == "cdg") state.cdgTrend else state.oryTrend
        val distance = if (type == "cdg") state.cdgDistance else state.oryDistance
        val travelTime = if (type == "cdg") state.cdgTravelTime else state.oryTravelTime
        val trafficStatus = if (type == "cdg") state.cdgTrafficStatus else state.oryTrafficStatus

        val isMaxTrans = state.isWidgetTransparent && state.isMaxTransparency
        val bgColorProvider = when {
            isMaxTrans -> ColorProvider(day = Color.Transparent, night = Color.Transparent)
            state.isWidgetTransparent -> ColorProvider(day = Color(context.getColor(R.color.widget_bg_glass)), night = Color(context.getColor(R.color.widget_bg_glass)))
            else -> ColorProvider(day = Color(context.getColor(R.color.widget_bg_solid)), night = Color(context.getColor(R.color.widget_bg_solid)))
        }

        val textColorHex = state.widgetTextColor
        val textCol = if (textColorHex != 0) Color(textColorHex) else null
        val mainTextProvider = textCol?.let { ColorProvider(day = it, night = it) } ?: ColorProvider(day = Color(context.getColor(R.color.widget_text_main)), night = Color(context.getColor(R.color.widget_text_main)))

        val trafficColor = when(trafficStatus) {
            1 -> ColorProvider(day = Color(0xFFFF9500), night = Color(0xFFFF9500))
            2 -> ColorProvider(day = Color(0xFFFF3B30), night = Color(0xFFFF3B30))
            else -> ColorProvider(day = Color(0xFF34C759), night = Color(0xFF34C759))
        }

        val isRefreshing = state.isRefreshing

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColorProvider)
                .cornerRadius(24.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = TextStyle(color = mainTextProvider, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                    
                    if (trend != 0) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = if (trend == 1) "↑" else "↓",
                            style = TextStyle(
                                color = if (trend == 1) ColorProvider(day = Color(0xFFFF3B30), night = Color(0xFFFF3B30)) else ColorProvider(day = Color(0xFF34C759), night = Color(0xFF34C759)),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (isRefreshing) {
                        Text(
                            text = "...",
                            style = TextStyle(color = mainTextProvider, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (travelTime != "--") {
                        Text(
                            text = travelTime,
                            style = TextStyle(color = trafficColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    val userFontSize = state.widgetFontSize.toFloat().sp
                    
                    Text(
                        text = formatMinutesWidget(minutes),
                        modifier = GlanceModifier.clickable(actionRunCallback<RefreshCallback>()),
                        style = TextStyle(color = ColorProvider(day = Color(context.getColor(R.color.widget_text_blue)), night = Color(context.getColor(R.color.widget_text_blue))), fontSize = userFontSize, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Row(
                        modifier = GlanceModifier
                            .background(ColorProvider(day = Color(context.getColor(R.color.widget_text_blue)), night = Color(context.getColor(R.color.widget_text_blue))))
                            .cornerRadius(12.dp)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable(actionRunCallback<NavigateCallback>(actionParametersOf(destKey to type))),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (distance > 0) "${String.format(Locale.US, "%.1f", distance)} km" else "Naviguer",
                            style = TextStyle(color = ColorProvider(day = Color.White, night = Color.White), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                val diffMins = (System.currentTimeMillis() - state.lastUpdated) / (1000 * 60)
                val timeLabel = when {
                    state.lastUpdated == 0L -> "--h--"
                    diffMins < 1 -> "À l'instant"
                    diffMins < 60 -> "Il y a ${diffMins}min"
                    else -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(state.lastUpdated))
                }

                Text(
                    text = timeLabel,
                    style = TextStyle(
                        color = if (state.isTaxiDataStale || diffMins > 10) ColorProvider(day = Color(0xFFFF9500), night = Color(0xFFFF9500)) else ColorProvider(day = Color.Gray, night = Color.Gray),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

class CdgWidget : BaseAirportWidget("cdg")
class OryWidget : BaseAirportWidget("ory")

class NewMetroWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = TaxiPrefs(context)
        provideContent {
            val state by prefs.taxiState.collectAsState(initial = TaxiState(0, 0, 0L, false))
            val allLines = parseAllMetros(state.metroData)
            val filteredLines = allLines.filter { state.enabledLines.contains(it.lineId) }
            NewMetroWidgetContent(state, filteredLines)
        }
    }

    @Composable
    private fun NewMetroWidgetContent(state: TaxiState, lines: List<LineStatus>) {
        val context = LocalContext.current
        val isMaxTrans = state.isWidgetTransparent && state.isMaxTransparency
        val bgColorProvider = when {
            isMaxTrans -> ColorProvider(day = Color.Transparent, night = Color.Transparent)
            state.isWidgetTransparent -> ColorProvider(day = Color(context.getColor(R.color.widget_bg_glass)), night = Color(context.getColor(R.color.widget_bg_glass)))
            else -> ColorProvider(day = Color(context.getColor(R.color.widget_bg_solid)), night = Color(context.getColor(R.color.widget_bg_solid)))
        }

        val textColorHex = state.widgetTextColor
        val textCol = if (textColorHex != 0) Color(textColorHex) else null
        val mainTextProvider = textCol?.let { ColorProvider(day = it, night = it) } ?: ColorProvider(day = Color(context.getColor(R.color.widget_text_main)), night = Color(context.getColor(R.color.widget_text_main)))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColorProvider)
                .cornerRadius(24.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Métro",
                    style = TextStyle(color = mainTextProvider, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (state.isMetroRefreshing) {
                    Text("...", style = TextStyle(color = mainTextProvider, fontSize = 12.sp))
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Column(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayLines = lines.take(18)
                val rows = displayLines.chunked(6)

                rows.forEach { rowLines ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowLines.forEach { line ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                MetroStatusCircle(line, 28.dp, 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MetroStatusCircle(line: LineStatus, size: Dp, fontSize: TextUnit) {
        val circleColor = when {
            line.severity == "BLOQUANTE" -> Color(0xFFFF3B30)
            line.isIncident -> Color(0xFFFFCC00)
            else -> Color(0xFF34C759)
        }

        Box(
            modifier = GlanceModifier
                .size(size)
                .background(circleColor)
                .cornerRadius(size / 2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = line.name.replace("Métro ", "").replace("RER ", ""),
                style = TextStyle(
                    color = ColorProvider(day = Color.White, night = Color.White),
                    fontSize = fontSize, 
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    private fun parseAllMetros(json: String): List<LineStatus> {
        if (json.isEmpty()) {
            return TrafficMapper.LINES_MAP.map { (id, name) ->
                LineStatus(id, name, "Normal", "", "INFO", false)
            }
        }
        return try {
            Json.decodeFromString<List<LineStatus>>(json)
        } catch (_: Exception) {
            TrafficMapper.LINES_MAP.map { (id, name) ->
                LineStatus(id, name, "Normal", "", "INFO", false)
            }
        }
    }
}

class CdgWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = CdgWidget() }
class OryWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = OryWidget() }
class NewMetroWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = NewMetroWidget() }

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        UpdateWorker.startImmediately(context)
    }
}

class NavigateCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val dest = parameters[destKey] ?: return
        val prefs = TaxiPrefs(context).taxiState.first()

        val lat = if (dest == "cdg") LocationTracker.CDG_LAT else LocationTracker.ORY_LAT
        val lon = if (dest == "cdg") LocationTracker.CDG_LON else LocationTracker.ORY_LON

        val uri = if (prefs.navigationApp == 1) {
            "waze://?ll=$lat,$lon&navigate=yes".toUri()
        } else {
            "google.navigation:q=$lat,$lon".toUri()
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/dir/?api=1&destination=$lat,$lon".toUri())
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}
