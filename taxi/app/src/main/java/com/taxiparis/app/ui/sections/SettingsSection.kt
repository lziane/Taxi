package com.taxiparis.app.ui.sections

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxiparis.app.domain.model.TaxiState
import com.taxiparis.app.ui.components.WidgetPreview

sealed class SettingsAction {
    data class ToggleLine(val id: String) : SettingsAction()
    data class SetTheme(val theme: Int) : SettingsAction()
    data class SetTransparency(val enabled: Boolean) : SettingsAction()
    data class SetMaxTransparency(val enabled: Boolean) : SettingsAction()
    data class SetNavigationApp(val choice: Int) : SettingsAction()
    data class SetThresholds(val cdg: Int, val ory: Int) : SettingsAction()
    data class SetFontSize(val size: Int) : SettingsAction()
    data class SetVibration(val enabled: Boolean) : SettingsAction()
    data class SetSmartRefresh(val enabled: Boolean, val distance: Int) : SettingsAction()
    data class SetWidgetTextColor(val color: Int) : SettingsAction()
    data class SetRefreshInterval(val minutes: Int) : SettingsAction()
    data class SetWorkingHours(val start: Int, val end: Int) : SettingsAction()
    object ClearCache : SettingsAction()
    object FullReset : SettingsAction()
    object ShowTutorial : SettingsAction()
    data class SetCGUAccepted(val accepted: Boolean) : SettingsAction()
}

@Composable
fun SettingsSection(appState: TaxiState, onViewLegal: () -> Unit, onAction: (SettingsAction) -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val openSettings = {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
    
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    var updateTrigger by remember { mutableIntStateOf(0) }
    
    DisposableEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                updateTrigger++
            }
        }
        val lifecycle = (context as? androidx.activity.ComponentActivity)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    var cdgT by remember(appState.cdgThreshold) { mutableFloatStateOf(appState.cdgThreshold.toFloat()) }
    var oryT by remember(appState.oryThreshold) { mutableFloatStateOf(appState.oryThreshold.toFloat()) }
    var workStart by remember(appState.workingStartHour) { mutableFloatStateOf(appState.workingStartHour.toFloat()) }
    var workEnd by remember(appState.workingEndHour) { mutableFloatStateOf(appState.workingEndHour.toFloat()) }
    var fontSizeState by remember(appState.widgetFontSize) { mutableFloatStateOf(appState.widgetFontSize.toFloat()) }

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Tout réinitialiser ?") },
            text = { Text("Cela effacera vos réglages, vos seuils d'alerte et vos préférences. Vous devrez accepter à nouveau les CGU.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(SettingsAction.FullReset)
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Réinitialiser") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Annuler") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            SettingsHeader("VOTRE WIDGET", "Aperçu en temps réel de votre configuration")
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                WidgetPreview(appState)
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            SettingsHeader("PERSONNALISATION", "Adaptez l'interface à votre goût")
            SettingsCard {
                Column {
                    SettingsRow("Thème de l'application", "Clair, Sombre ou Automatique", Icons.Rounded.Palette, Color(0xFF5856D6)) {
                        TextButton(onClick = { onAction(SettingsAction.SetTheme((appState.appTheme + 1) % 3)) }) {
                            Text(
                                text = when(appState.appTheme) { 1 -> "Clair" 2 -> "Sombre" else -> "Système" },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsRow("Transparence Widget", "Effet de verre sur l'écran d'accueil", Icons.Rounded.Opacity, Color(0xFF5AC8FA)) {
                        Switch(checked = appState.isWidgetTransparent, onCheckedChange = { onAction(SettingsAction.SetTransparency(it)) })
                    }
                    if (appState.isWidgetTransparent) {
                        SettingsRow("Transparence Totale", "Fond 100% invisible", Icons.Rounded.BlurOff, Color(0xFF007AFF)) {
                            Switch(checked = appState.isMaxTransparency, onCheckedChange = { onAction(SettingsAction.SetMaxTransparency(it)) })
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    ModernSettingsSlider(
                        label = "Taille texte Widget",
                        value = fontSizeState,
                        unit = "sp",
                        valueRange = 12f..60f,
                        onValueChange = { fontSizeState = it },
                        onValueChangeFinished = { onAction(SettingsAction.SetFontSize(fontSizeState.toInt())) }
                    )
                }
            }
        }

        item {
            SettingsHeader("OPÉRATIONNEL", "Contrôlez quand l'app travaille pour vous")
            SettingsCard {
                Column {
                    SettingsRow(
                        label = "Smart Refresh",
                        subtitle = "Économise la batterie si vous êtes loin de Paris",
                        icon = Icons.Rounded.BatteryChargingFull,
                        iconColor = Color(0xFF34C759)
                    ) {
                        Switch(checked = appState.smartRefreshEnabled, onCheckedChange = { onAction(SettingsAction.SetSmartRefresh(it, appState.smartRefreshDistance)) })
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                    Text(
                        text = "Plage de service : ${workStart.toInt()}h - ${workEnd.toInt()}h",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    ModernSettingsSlider(
                        label = "Début du service",
                        value = workStart,
                        unit = "h",
                        valueRange = 0f..23f,
                        onValueChange = { workStart = it },
                        onValueChangeFinished = { onAction(SettingsAction.SetWorkingHours(workStart.toInt(), workEnd.toInt())) }
                    )
                    ModernSettingsSlider(
                        label = "Fin du service",
                        value = workEnd,
                        unit = "h",
                        valueRange = 1f..24f,
                        onValueChange = { workEnd = it },
                        onValueChangeFinished = { onAction(SettingsAction.SetWorkingHours(workStart.toInt(), workEnd.toInt())) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                    ModernSettingsSlider(
                        label = "Intervalle de mise à jour",
                        value = appState.refreshInterval.toFloat(),
                        unit = "min",
                        valueRange = 15f..120f,
                        onValueChange = { onAction(SettingsAction.SetRefreshInterval(it.toInt())) },
                        onValueChangeFinished = {}
                    )
                }
            }
        }

        item {
            SettingsHeader("NAVIGATION", "Votre outil de guidage favori")
            SettingsCard {
                SettingsRow(
                    label = "Application GPS",
                    subtitle = "Lancer le trajet depuis le widget",
                    icon = Icons.Rounded.Navigation,
                    iconColor = Color(0xFF007AFF)
                ) {
                    TextButton(onClick = { onAction(SettingsAction.SetNavigationApp(1 - appState.navigationApp)) }) {
                        Text(
                            text = if (appState.navigationApp == 0) "Google Maps" else "Waze",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            SettingsHeader("ALERTES", "Soyez prévenu dès que l'attente grimpe")
            SettingsCard {
                Column {
                    SettingsRow(
                        label = "Vibrations",
                        subtitle = "Alerte tactile lors des notifications",
                        icon = Icons.Rounded.Vibration,
                        iconColor = Color(0xFF8E8E93)
                    ) {
                        Switch(
                            checked = appState.vibrationEnabled,
                            onCheckedChange = { onAction(SettingsAction.SetVibration(it)) }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    ModernSettingsSlider(
                        label = "Seuil CDG",
                        value = cdgT,
                        unit = "min",
                        valueRange = 0f..180f,
                        onValueChange = { cdgT = it },
                        onValueChangeFinished = { onAction(SettingsAction.SetThresholds(cdgT.toInt(), oryT.toInt())) }
                    )
                    ModernSettingsSlider(
                        label = "Seuil Orly",
                        value = oryT,
                        unit = "min",
                        valueRange = 0f..180f,
                        onValueChange = { oryT = it },
                        onValueChangeFinished = { onAction(SettingsAction.SetThresholds(cdgT.toInt(), oryT.toInt())) }
                    )
                }
            }
        }

        item {
            key(updateTrigger) {
                val hasGPS = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasBG = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true
                val hasNotif = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true
                
                SettingsHeader("DIAGNOSTIC", "Vérifiez que tout fonctionne correctement")
                SettingsCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DiagnosticRow("Localisation GPS", hasGPS) {
                            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                        DiagnosticRow("Position Arrière-plan", hasBG) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        }
                        DiagnosticRow("Notifications", hasNotif) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                openSettings()
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = openSettings,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Settings, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Réglages", fontSize = 13.sp)
                            }
                            
                            Button(
                                onClick = { showResetDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.RestartAlt, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Réinitialiser", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsHeader("LÉGAL", "Vos données et nos engagements")
            SettingsCard {
                Column {
                    TextButton(
                        onClick = onViewLegal,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.Gavel, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Consulter les CGU", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Taxi Paris version 1.0.0\nDéveloppé pour les taxis parisiens.",
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SettingsHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun SettingsRow(
    label: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.padding(8.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
fun DiagnosticRow(label: String, ok: Boolean, onRequestPermission: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!ok) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30).copy(alpha = 0.1f), contentColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text("Requis", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Text(
                    "Actif",
                    color = Color(0xFF34C759),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                contentDescription = null,
                tint = if (ok) Color(0xFF34C759) else Color(0xFFFF3B30),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSettingsSlider(
    label: String,
    value: Float,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    color: Color = Color(0xFF007AFF)
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "$label : ${value.toInt()} $unit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            thumb = {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(12.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = color,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                )
            }
        )
    }
}
