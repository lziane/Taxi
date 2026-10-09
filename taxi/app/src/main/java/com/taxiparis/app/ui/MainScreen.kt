package com.taxiparis.app.ui

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxiparis.app.ui.components.*
import com.taxiparis.app.ui.sections.*
import kotlinx.coroutines.launch

/**
 * Represents the primary navigation sections of the application.
 */
enum class Screen {
    Airports, Metro, Settings
}

/**
 * The root composable of the application.
 * Manages the top-level navigation flow (CGU -> Tutorial -> Permissions -> Main).
 */
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var currentScreen by rememberSaveable { mutableStateOf(Screen.Airports) }
    var viewingCGUInSettings by rememberSaveable { mutableStateOf(false) }

    // Déclencher le rafraîchissement initial une fois que les permissions sont prêtes
    LaunchedEffect(uiState.taxiState.hasAcceptedCGU, uiState.taxiState.showOnboarding) {
        if (uiState.taxiState.hasAcceptedCGU && !uiState.taxiState.showOnboarding) {
            viewModel.refreshAll()
        }
    }

    val isDark = when(uiState.taxiState.appTheme) {
        1 -> false
        2 -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    com.taxiparis.app.ui.theme.TaxiTheme(darkTheme = isDark) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AnimatedContent(
                targetState = when {
                    !uiState.taxiState.hasAcceptedCGU -> "cgu"
                    uiState.taxiState.showOnboarding -> "tutorial"
                    else -> {
                        val fineLocationGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        val needsBackground = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
                                !uiState.taxiState.hasSeenLocationOnboarding

                        val needsNotifications = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                                androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
                                !uiState.taxiState.hasSeenNotificationOnboarding

                        if (!fineLocationGranted || needsBackground || needsNotifications) "permissions" else "main"
                    }
                },
                transitionSpec = {
                    (fadeIn(tween(durationMillis = 500, easing = EaseOutExpo)) + scaleIn(animationSpec = tween(durationMillis = 500, easing = EaseOutExpo), initialScale = 0.92f))
                        .togetherWith(fadeOut(tween(durationMillis = 400, easing = EaseInExpo)))
                },
                label = "RootTransition"
            ) { targetState ->
                when (targetState) {
                    "cgu" -> LegalScreen(isMandatory = true, onAccept = { viewModel.setCGUAccepted(true) }, onBack = {})
                    "tutorial" -> OnboardingTutorial(onFinish = { viewModel.setOnboardingCompleted(true) })
                    "permissions" -> {
                        val fineLocationGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val needsBackground = !uiState.taxiState.hasSeenLocationOnboarding
                        val needsNotifications = !uiState.taxiState.hasSeenNotificationOnboarding

                        UnifiedPermissionScreen(
                            fineLocationGranted = fineLocationGranted,
                            needsBackground = needsBackground,
                            needsNotifications = needsNotifications,
                            onDone = {
                                viewModel.setLocationOnboardingSeen(true)
                                viewModel.setNotificationOnboardingSeen(true)
                                viewModel.refreshAll()
                            }
                        )
                    }
                    "main" -> {
                        if (viewingCGUInSettings) {
                            LegalScreen(
                                isMandatory = false, 
                                onAccept = { viewingCGUInSettings = false },
                                onBack = { viewingCGUInSettings = false },
                                onRevoke = {
                                    viewModel.setCGUAccepted(false)
                                    viewModel.clearCache()
                                    viewingCGUInSettings = false
                                }
                            )
                        } else {
                            MainScaffold(
                                currentScreen = currentScreen,
                                onScreenChange = { currentScreen = it },
                                uiState = uiState,
                                viewModel = viewModel,
                                onViewLegal = { viewingCGUInSettings = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The main application layout with TopBar, BottomBar and HorizontalPager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    uiState: TaxiUiState,
    viewModel: MainViewModel,
    onViewLegal: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = currentScreen.ordinal, pageCount = { Screen.entries.size })

    LaunchedEffect(pagerState.currentPage) {
        if (Screen.entries[pagerState.currentPage] != currentScreen) {
            onScreenChange(Screen.entries[pagerState.currentPage])
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = when(Screen.entries[pagerState.currentPage]) {
                            Screen.Airports -> "Aéroport"
                            Screen.Metro -> "Train"
                            Screen.Settings -> "Paramètres"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    if (Screen.entries[pagerState.currentPage] != Screen.Settings) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.refreshAll(includeTraffic = Screen.entries[pagerState.currentPage] == Screen.Metro)
                        }) {
                            Icon(Icons.Rounded.Refresh, "Rafraîchir")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), contentAlignment = Alignment.Center) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier.fillMaxWidth(0.85f).height(72.dp).border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(40.dp)).shadow(12.dp, RoundedCornerShape(40.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        PillNavItem(Screen.entries[pagerState.currentPage] == Screen.Airports, { scope.launch { pagerState.animateScrollToPage(Screen.Airports.ordinal) } }, Icons.Rounded.AirplanemodeActive, "Aéroport")
                        PillNavItem(Screen.entries[pagerState.currentPage] == Screen.Metro, { scope.launch { pagerState.animateScrollToPage(Screen.Metro.ordinal) } }, Icons.Rounded.Train, "Train")
                        PillNavItem(Screen.entries[pagerState.currentPage] == Screen.Settings, { scope.launch { pagerState.animateScrollToPage(Screen.Settings.ordinal) } }, Icons.Rounded.Settings, "Paramètres")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
                when (Screen.entries[page]) {
                    Screen.Airports -> AirportSection(uiState)
                    Screen.Metro -> MetroSection(uiState.trafficState, { viewModel.refreshAll(includeTraffic = true) }, uiState.taxiState.enabledLines)
                    Screen.Settings -> SettingsSection(uiState.taxiState, onViewLegal) { action ->
                        handleSettingsAction(action, viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Handles actions from the Settings section and updates widgets.
 */
private fun handleSettingsAction(action: SettingsAction, viewModel: MainViewModel) {
    when(action) {
        is SettingsAction.ToggleLine -> viewModel.toggleLine(action.id)
        is SettingsAction.SetTheme -> viewModel.setAppTheme(action.theme)
        is SettingsAction.SetTransparency -> viewModel.setWidgetTransparent(action.enabled)
        is SettingsAction.SetMaxTransparency -> viewModel.setMaxTransparency(action.enabled)
        is SettingsAction.SetNavigationApp -> viewModel.setNavigationApp(action.choice)
        is SettingsAction.SetThresholds -> viewModel.setThresholds(action.cdg, action.ory)
        is SettingsAction.SetFontSize -> viewModel.setWidgetFontSize(action.size)
        is SettingsAction.SetVibration -> viewModel.setVibrationEnabled(action.enabled)
        is SettingsAction.SetSmartRefresh -> viewModel.setSmartRefresh(action.enabled, action.distance)
        is SettingsAction.SetWidgetTextColor -> viewModel.setWidgetTextColor(action.color)
        is SettingsAction.SetRefreshInterval -> viewModel.setRefreshInterval(action.minutes)
        is SettingsAction.SetWorkingHours -> viewModel.setWorkingHours(action.start, action.end)
        is SettingsAction.ClearCache -> { viewModel.clearCache(); viewModel.refreshAll(includeTraffic = true) }
        SettingsAction.FullReset -> viewModel.fullReset()
        SettingsAction.ShowTutorial -> viewModel.setOnboardingCompleted(false)
        is SettingsAction.SetCGUAccepted -> viewModel.setCGUAccepted(action.accepted)
    }
    viewModel.updateWidgets()
}

@Composable
fun PillNavItem(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Column(
        modifier = Modifier.clip(RoundedCornerShape(32.dp)).clickable(onClick = onClick).sizeIn(minWidth = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        Text(label, style = TextStyle(fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = color))
    }
}
