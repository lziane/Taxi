package com.taxiparis.app.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import com.taxiparis.app.domain.model.TaxiState
import com.taxiparis.app.ui.TaxiUiState
import com.taxiparis.app.ui.util.formatMinutes
import androidx.compose.foundation.shape.RoundedCornerShape
import com.taxiparis.app.domain.model.Flight
import kotlinx.serialization.json.Json
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.People

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportSection(uiState: TaxiUiState) {
    val taxiState = uiState.taxiState
    val flights = try {
        if (taxiState.upcomingFlights.isNotEmpty()) {
            Json.decodeFromString<List<Flight>>(taxiState.upcomingFlights)
        } else emptyList()
    } catch (_: Exception) { emptyList() }

    var selectedFlight by remember { mutableStateOf<Flight?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet && selectedFlight != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FlightDetailContent(selectedFlight!!)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { WeatherHeaderCard(taxiState) }
        
        item {
             Text(
                text = "Temps d'attente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SquareTaxiCard("CDG", taxiState.cdgMinutes, taxiState.cdgDistance, taxiState.cdgTravelTime, taxiState.cdgTrafficStatus, taxiState.cdgTrend, Modifier.weight(1f))
                SquareTaxiCard("ORL", taxiState.oryMinutes, taxiState.oryDistance, taxiState.oryTravelTime, taxiState.oryTrafficStatus, taxiState.oryTrend, Modifier.weight(1f))
            }
        }

        if (flights.isNotEmpty()) {
            item {
                Text(
                    text = "Arrivées Stratégiques",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(flights.take(6)) { flight ->
                FlightArrivalCard(flight) {
                    selectedFlight = flight
                    showSheet = true
                }
            }
        }
    }
}

@Composable
fun FlightArrivalCard(flight: Flight, onClick: () -> Unit) {
    val isTop = flight.score >= 80
    
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.size(48.dp).border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val domain = when(flight.airline.lowercase()) {
                        "air france" -> "airfrance.fr"
                        "emirates" -> "emirates.com"
                        "qatar airways" -> "qatarairways.com"
                        "lufthansa" -> "lufthansa.com"
                        "easyjet" -> "easyjet.com"
                        "delta" -> "delta.com"
                        "american airlines" -> "aa.com"
                        "british airways" -> "britishairways.com"
                        else -> "aviation-edge.com"
                    }
                    AsyncImage(
                        model = "https://logo.clearbit.com/$domain",
                        contentDescription = "Logo ${flight.airline}",
                        modifier = Modifier.size(32.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_directions)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = flight.flightNo,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "• ${flight.origin}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "T${flight.terminal}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = " • ${flight.passengers} passagers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = flight.arrivalTimeFrance,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Arr. France",
                    style = TextStyle(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                )
                if (isTop) {
                    Surface(
                        color = Color(0xFFFF3B30),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "TOP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlightDetailContent(flight: Flight) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(70.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (flight.aircraftImageUrl != null) {
                        AsyncImage(
                            model = flight.aircraftImageUrl,
                            contentDescription = "Image de l'avion",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.AirplanemodeActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = flight.flightNo, 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(flight.airline, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DÉPART", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(flight.departureTime, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(flight.origin.substringAfter("(").substringBefore(")"), style = MaterialTheme.typography.bodySmall)
                }
                
                Icon(Icons.Rounded.AirplanemodeActive, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ARRIVÉE (FR)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(flight.arrivalTimeFrance, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(flight.airport, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoSmallCard(Icons.Rounded.People, "${flight.passengers}", "Passagers", Modifier.weight(1f))
            InfoSmallCard(Icons.Rounded.AccessTime, flight.delay ?: "À l'heure", "Statut", Modifier.weight(1f), isAlert = flight.delay != null)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (flight.aircraftType != null) {
                DetailRow(Icons.Rounded.AirplanemodeActive, "Appareil", flight.aircraftType)
            }
            if (flight.importantInfo != null) {
                DetailRow(Icons.Rounded.Info, "Note stratégique", flight.importantInfo, isHighlight = true)
            }
        }
    }
}

@Composable
fun InfoSmallCard(icon: ImageVector, value: String, label: String, modifier: Modifier, isAlert: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isAlert) Color(0xFFFF3B30).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (isAlert) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, color = if (isAlert) Color(0xFFFF3B30) else MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String, isHighlight: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(20.dp).padding(top = 2.dp), tint = if (isHighlight) Color(0xFFFF9500) else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun WeatherHeaderCard(taxiState: TaxiState) {
    val tempStr = taxiState.weatherTemp
    val temp = tempStr.replace("°C", "").toIntOrNull() ?: 0
    
    val (bgColor, desc, textColor) = when {
        temp >= 25 -> Triple(Color(0xFFFF3B30), "Canicule", Color.Black)
        taxiState.weatherDesc == "Ensoleillé" -> Triple(Color(0xFFFFCC00), "Ensoleillé", Color.Black)
        else -> Triple(Color(0xFF1C1C45), taxiState.weatherDesc, Color.White)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                text = taxiState.weatherTemp,
                style = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Bold, color = textColor)
            )
            Text(text = "Paris", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, color = textColor))
        }

        Column(modifier = Modifier.align(Alignment.CenterEnd), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.WbSunny,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(70.dp)
            )
            Text(text = desc, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor))
        }
    }
}

@Composable
fun SquareTaxiCard(title: String, minutes: Int, distance: Double, travelTime: String, trafficStatus: Int, trend: Int, modifier: Modifier) {
    val trafficColor = when(trafficStatus) {
        1 -> Color(0xFFFF9500)
        2 -> Color(0xFFFF3B30)
        else -> Color(0xFF34C759)
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(32.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Aéroport $title, attente ${formatMinutes(minutes)}, temps de trajet $travelTime"
            },
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                )
                if (trend != 0) {
                    Icon(
                        imageVector = if (trend == 1) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                        contentDescription = if (trend == 1) "En hausse" else "En baisse",
                        modifier = Modifier.padding(start = 4.dp).size(16.dp),
                        tint = if (trend == 1) Color(0xFFFF3B30) else Color(0xFF34C759)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatMinutes(minutes),
                    style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "attente",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = trafficColor
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = travelTime,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = trafficColor)
                    )
                }
                
                if (distance > 0) {
                    Text(
                        text = String.format(java.util.Locale.FRANCE, "%.1f km", distance),
                        style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
