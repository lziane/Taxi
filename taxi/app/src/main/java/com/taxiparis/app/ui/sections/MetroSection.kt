package com.taxiparis.app.ui.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxiparis.app.domain.model.LineStatus
import com.taxiparis.app.ui.TrafficState

@Composable
fun MetroSection(state: TrafficState, onRefresh: () -> Unit, enabledLines: Set<String>?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state is TrafficState.Success) {
                        val filteredLines = state.lines.filter { enabledLines?.contains(it.lineId) ?: true }
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            filteredLines.chunked(7).forEach { rowLines ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowLines.forEach { line ->
                                        ModernMetroCircle(line)
                                    }
                                }
                            }
                        }
                    } else if (state is TrafficState.Error) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                            Button(
                                onClick = onRefresh,
                                modifier = Modifier.padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                            ) {
                                Text("Réessayer", color = Color.White)
                            }
                        }
                    } else {
                        CircularProgressIndicator(color = Color(0xFF007AFF))
                    }
                }
            }
        }

        item {
            Text(
                text = "Perturbation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (state is TrafficState.Success) {
            val incidents = state.lines.filter { it.isIncident }
            if (incidents.isEmpty()) {
                item {
                    Text("Aucune perturbation signalée.", color = Color.Gray)
                }
            } else {
                items(incidents) { line ->
                    PerturbationCard(line)
                }
            }
        }
    }
}

@Composable
fun ModernMetroCircle(line: LineStatus) {
    val color = when {
        line.severity == "BLOQUANTE" -> Color(0xFFFF3B30)
        line.isIncident -> Color(0xFFFFCC00)
        else -> Color(0xFF34C759)
    }

    Surface(
        modifier = Modifier.padding(4.dp).size(38.dp)
            .semantics { contentDescription = "Ligne ${line.name}, état : ${line.status}" },
        shape = CircleShape,
        color = color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = line.name.replace("Métro ", "").replace("RER ", ""),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PerturbationCard(line: LineStatus) {
    val color = if (line.severity == "BLOQUANTE") Color(0xFFFF3B30) else Color(0xFFFFCC00)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = line.name.replace("Métro ", "").replace("RER ", ""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = line.status,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (line.message.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = line.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
        }
    }
}
