package com.taxiparis.app.domain.model

import com.taxiparis.app.data.mapper.TrafficMapper

data class TaxiState(
    val cdgMinutes: Int,
    val oryMinutes: Int,
    val lastUpdated: Long,
    val isForceEnabled: Boolean,
    val widgetFontSize: Int = 32,
    val isWidgetTransparent: Boolean = false,
    val cdgThreshold: Int = 0,
    val oryThreshold: Int = 0,
    val lastNotifiedTime: Long = 0L,
    val vibrationEnabled: Boolean = true,
    val appTheme: Int = 0, // 0: System, 1: Light, 2: Dark
    val weatherTemp: String = "--",
    val weatherDesc: String = "Inconnu",
    val weatherCity: String = "Paris",
    val metroData: String = "",
    val lastPrimFetchTimestamp: Long = 0L,
    val enabledLines: Set<String> = TrafficMapper.LINES_MAP.keys,
    val navigationApp: Int = 0, // 0: Google Maps, 1: Waze
    val cdgDistance: Double = 0.0,
    val oryDistance: Double = 0.0,
    val isMaxTransparency: Boolean = false,
    val widgetTextColor: Int = 0,
    val isRefreshing: Boolean = false,
    val isTaxiDataStale: Boolean = false,
    val smartRefreshEnabled: Boolean = true,
    val smartRefreshDistance: Int = 50, // km
    val showOnboarding: Boolean = true,
    val workingStartHour: Int = 0,
    val workingEndHour: Int = 24,
    val refreshInterval: Int = 15, // minutes
    val cdgTravelTime: String = "--",
    val oryTravelTime: String = "--",
    val cdgTrafficStatus: Int = 0, // 0: Normal, 1: Moderate, 2: Heavy
    val oryTrafficStatus: Int = 0,
    val isMetroRefreshing: Boolean = false,
    val hasAcceptedCGU: Boolean = false,
    val hasSeenLocationOnboarding: Boolean = false,
    val hasSeenNotificationOnboarding: Boolean = false,
    val cdgTrend: Int = 0, // 0: stable, 1: up, 2: down
    val oryTrend: Int = 0,
    val upcomingFlights: String = "" // JSON list of flights
)
