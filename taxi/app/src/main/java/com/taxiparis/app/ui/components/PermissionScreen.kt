package com.taxiparis.app.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A unified screen to request all necessary system permissions.
 */
@Composable
fun UnifiedPermissionScreen(
    fineLocationGranted: Boolean,
    needsBackground: Boolean,
    needsNotifications: Boolean,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    
    var locGranted by remember { mutableStateOf(fineLocationGranted) }
    var bgGranted by remember { mutableStateOf(!needsBackground) }
    var notifGranted by remember { mutableStateOf(!needsNotifications) }

    DisposableEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                locGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                             androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    notifGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    bgGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
            }
        }
        val lifecycle = (context as? androidx.activity.ComponentActivity)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                     perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notifGranted = perms[Manifest.permission.POST_NOTIFICATIONS] == true
        }
    }

    val bgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        bgGranted = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Configuration finale",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Pour fonctionner, Taxi Paris a besoin de quelques accès essentiels. Vos données de position restent privées et locales.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(40.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200, easing = EaseOutBack)) { it / 2 }
            ) {
                PermissionItem(
                    title = "Position GPS",
                    desc = "Calcul de la distance vers CDG/Orly et trafic en temps réel.",
                    icon = Icons.Rounded.LocationOn,
                    granted = locGranted,
                    onClick = {
                        launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                )
            }
            
            if (locGranted && needsNotifications && !notifGranted) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(500)) + expandVertically()
                ) {
                    PermissionItem(
                        title = "Notifications",
                        desc = "Alertes d'attente élevée même si l'application est fermée.",
                        icon = Icons.Rounded.Notifications,
                        granted = notifGranted,
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                            }
                        }
                    )
                }
            }
            
            if (locGranted && needsBackground && !bgGranted) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(500)) + expandVertically()
                ) {
                    PermissionItem(
                        title = "Position Arrière-plan",
                        desc = "Mise à jour automatique des widgets et Smart Refresh (batterie).",
                        icon = Icons.Rounded.Sync,
                        granted = bgGranted,
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        }
                    )
                }
            }
        }
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Note sur la vie privée : Aucune donnée de position n'est transmise à nos serveurs. Tout est traité sur votre téléphone.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onDone,
            enabled = locGranted,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (locGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                if (locGranted && bgGranted && notifGranted) "Terminer" else "Continuer",
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    desc: String,
    icon: ImageVector,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (granted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
            .clickable(enabled = !granted, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = if (granted) Color(0xFF34C759).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (granted) Icons.Rounded.Check else icon,
                    contentDescription = null,
                    tint = if (granted) Color(0xFF34C759) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (granted) Color(0xFF34C759) else MaterialTheme.colorScheme.onBackground)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (!granted) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun NotificationConsentDialog(onDismiss: () -> Unit, onGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restez alerté", fontWeight = FontWeight.Bold) },
        text = {
            Text("Taxi Paris peut vous envoyer des notifications pour vous prévenir quand l'attente dépasse vos seuils personnalisés.")
        },
        confirmButton = {
            Button(onClick = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }) { Text("Autoriser") }
        }
    )
}

@Composable
fun BackgroundLocationConsentDialog(onDismiss: () -> Unit, onGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Position Arrière-plan", fontWeight = FontWeight.Bold) },
        text = {
            Text("Nécessaire pour le Smart Refresh et les mises à jour auto du widget.")
        },
        confirmButton = {
            Button(onClick = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }) { Text("Autoriser") }
        }
    )
}

@Composable
fun PermissionOnboarding(onGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) onGranted()
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.LocationOn, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(32.dp))
            Text("Besoin du GPS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(48.dp))
            Button(onClick = {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Autoriser")
            }
        }
    }
}
