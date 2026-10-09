package com.taxiparis.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.taxiparis.app.data.mapper.TrafficMapper
import com.taxiparis.app.domain.model.TaxiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "taxi_prefs")

class TaxiPrefs(context: Context) {
    val context = context.applicationContext

    companion object {
        private val CDG_MINUTES = intPreferencesKey("cdg_minutes")
        private val ORY_MINUTES = intPreferencesKey("ory_minutes")
        private val LAST_UPDATED = longPreferencesKey("last_updated")
        private val IS_FORCE_ENABLED = booleanPreferencesKey("is_force_enabled")
        private val WIDGET_FONT_SIZE = intPreferencesKey("widget_font_size")
        private val IS_WIDGET_TRANSPARENT = booleanPreferencesKey("is_widget_transparent")
        private val CDG_THRESHOLD = intPreferencesKey("cdg_threshold")
        private val ORY_THRESHOLD = intPreferencesKey("ory_threshold")
        private val LAST_NOTIFIED_TIME = longPreferencesKey("last_notified_time")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val APP_THEME = intPreferencesKey("app_theme")
        private val WEATHER_TEMP = stringPreferencesKey("weather_temp")
        private val WEATHER_DESC = stringPreferencesKey("weather_desc")
        private val WEATHER_CITY = stringPreferencesKey("weather_city")
        private val METRO_DATA = stringPreferencesKey("metro_data")
        private val LAST_PRIM_FETCH_TIMESTAMP = longPreferencesKey("last_prim_fetch_timestamp")
        private val ENABLED_LINES = stringPreferencesKey("enabled_lines")
        private val NAVIGATION_APP = intPreferencesKey("navigation_app")
        private val CDG_DISTANCE = stringPreferencesKey("cdg_distance")
        private val ORY_DISTANCE = stringPreferencesKey("ory_distance")
        private val IS_MAX_TRANSPARENCY = booleanPreferencesKey("is_max_transparency")
        private val WIDGET_TEXT_COLOR = intPreferencesKey("widget_text_color")
        private val IS_REFRESHING = booleanPreferencesKey("is_refreshing")
        private val IS_TAXI_DATA_STALE = booleanPreferencesKey("is_taxi_data_stale")
        private val SMART_REFRESH_ENABLED = booleanPreferencesKey("smart_refresh_enabled")
        private val SMART_REFRESH_DISTANCE = intPreferencesKey("smart_refresh_distance")
        private val SHOW_ONBOARDING = booleanPreferencesKey("show_onboarding")
        private val WORKING_START_HOUR = intPreferencesKey("working_start_hour")
        private val WORKING_END_HOUR = intPreferencesKey("working_end_hour")
        private val REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        private val CDG_TRAVEL_TIME = stringPreferencesKey("cdg_travel_time")
        private val ORY_TRAVEL_TIME = stringPreferencesKey("ory_travel_time")
        private val CDG_TRAFFIC_STATUS = intPreferencesKey("cdg_traffic_status")
        private val ORY_TRAFFIC_STATUS = intPreferencesKey("ory_traffic_status")
        private val IS_METRO_REFRESHING = booleanPreferencesKey("is_metro_refreshing")
        private val HAS_ACCEPTED_CGU = booleanPreferencesKey("has_accepted_cgu")
        private val HAS_SEEN_LOCATION_ONBOARDING = booleanPreferencesKey("has_seen_location_onboarding")
        private val HAS_SEEN_NOTIFICATION_ONBOARDING = booleanPreferencesKey("has_seen_notification_onboarding")
        private val CDG_TREND = intPreferencesKey("cdg_trend")
        private val ORY_TREND = intPreferencesKey("ory_trend")
        private val UPCOMING_FLIGHTS = stringPreferencesKey("upcoming_flights")
    }

    val taxiState: Flow<TaxiState> = context.dataStore.data.map { prefs ->
        TaxiState(
            cdgMinutes = prefs[CDG_MINUTES] ?: 0,
            oryMinutes = prefs[ORY_MINUTES] ?: 0,
            lastUpdated = prefs[LAST_UPDATED] ?: 0L,
            isForceEnabled = prefs[IS_FORCE_ENABLED] ?: false,
            widgetFontSize = prefs[WIDGET_FONT_SIZE] ?: 32,
            isWidgetTransparent = prefs[IS_WIDGET_TRANSPARENT] ?: false,
            cdgThreshold = prefs[CDG_THRESHOLD] ?: 0,
            oryThreshold = prefs[ORY_THRESHOLD] ?: 0,
            lastNotifiedTime = prefs[LAST_NOTIFIED_TIME] ?: 0L,
            vibrationEnabled = prefs[VIBRATION_ENABLED] ?: true,
            appTheme = prefs[APP_THEME] ?: 0,
            weatherTemp = prefs[WEATHER_TEMP] ?: "--",
            weatherDesc = prefs[WEATHER_DESC] ?: "Inconnu",
            weatherCity = prefs[WEATHER_CITY] ?: "Paris",
            metroData = prefs[METRO_DATA] ?: "",
            lastPrimFetchTimestamp = prefs[LAST_PRIM_FETCH_TIMESTAMP] ?: 0L,
            enabledLines = prefs[ENABLED_LINES]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: TrafficMapper.LINES_MAP.keys,
            navigationApp = prefs[NAVIGATION_APP] ?: 0,
            cdgDistance = prefs[CDG_DISTANCE]?.toDoubleOrNull() ?: 0.0,
            oryDistance = prefs[ORY_DISTANCE]?.toDoubleOrNull() ?: 0.0,
            isMaxTransparency = prefs[IS_MAX_TRANSPARENCY] ?: false,
            widgetTextColor = prefs[WIDGET_TEXT_COLOR] ?: 0,
            isRefreshing = prefs[IS_REFRESHING] ?: false,
            isTaxiDataStale = prefs[IS_TAXI_DATA_STALE] ?: false,
            smartRefreshEnabled = prefs[SMART_REFRESH_ENABLED] ?: true,
            smartRefreshDistance = prefs[SMART_REFRESH_DISTANCE] ?: 50,
            showOnboarding = prefs[SHOW_ONBOARDING] ?: true,
            workingStartHour = prefs[WORKING_START_HOUR] ?: 0,
            workingEndHour = prefs[WORKING_END_HOUR] ?: 24,
            refreshInterval = prefs[REFRESH_INTERVAL] ?: 15,
            cdgTravelTime = prefs[CDG_TRAVEL_TIME] ?: "--",
            oryTravelTime = prefs[ORY_TRAVEL_TIME] ?: "--",
            cdgTrafficStatus = prefs[CDG_TRAFFIC_STATUS] ?: 0,
            oryTrafficStatus = prefs[ORY_TRAFFIC_STATUS] ?: 0,
            isMetroRefreshing = prefs[IS_METRO_REFRESHING] ?: false,
            hasAcceptedCGU = prefs[HAS_ACCEPTED_CGU] ?: false,
            hasSeenLocationOnboarding = prefs[HAS_SEEN_LOCATION_ONBOARDING] ?: false,
            hasSeenNotificationOnboarding = prefs[HAS_SEEN_NOTIFICATION_ONBOARDING] ?: false,
            cdgTrend = prefs[CDG_TREND] ?: 0,
            oryTrend = prefs[ORY_TREND] ?: 0,
            upcomingFlights = prefs[UPCOMING_FLIGHTS] ?: ""
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[SHOW_ONBOARDING] = !completed }
    }

    suspend fun setCGUAccepted(accepted: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[HAS_ACCEPTED_CGU] = accepted }
    }

    suspend fun setLocationOnboardingSeen(seen: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[HAS_SEEN_LOCATION_ONBOARDING] = seen }
    }

    suspend fun setNotificationOnboardingSeen(seen: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[HAS_SEEN_NOTIFICATION_ONBOARDING] = seen }
    }

    suspend fun setMetroRefreshing(refreshing: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[IS_METRO_REFRESHING] = refreshing }
    }

    suspend fun setRefreshing(refreshing: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[IS_REFRESHING] = refreshing }
    }

    suspend fun setMaxTransparency(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[IS_MAX_TRANSPARENCY] = enabled }
    }

    suspend fun setWidgetTextColor(color: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[WIDGET_TEXT_COLOR] = color }
    }

    suspend fun updateDistances(cdg: Double, ory: Double) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[CDG_DISTANCE] = cdg.toString()
            it[ORY_DISTANCE] = ory.toString()
        }
    }

    suspend fun setNavigationApp(choice: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[NAVIGATION_APP] = choice }
    }

    suspend fun updateWaitTimes(cdg: Int, ory: Int, isStale: Boolean = false) = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            val oldCdg = prefs[CDG_MINUTES] ?: 0
            val oldOry = prefs[ORY_MINUTES] ?: 0
            
            val newCdgTrend = when {
                cdg > oldCdg -> 1
                cdg < oldCdg -> 2
                else -> 0
            }
            val newOryTrend = when {
                ory > oldOry -> 1
                ory < oldOry -> 2
                else -> 0
            }

            prefs[CDG_MINUTES] = cdg
            prefs[ORY_MINUTES] = ory
            prefs[CDG_TREND] = newCdgTrend
            prefs[ORY_TREND] = newOryTrend
            prefs[LAST_UPDATED] = System.currentTimeMillis()
            prefs[IS_TAXI_DATA_STALE] = isStale
        }
    }

    suspend fun updateTrafficInfo(cdgTime: String, cdgStatus: Int, oryTime: String, oryStatus: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[CDG_TRAVEL_TIME] = cdgTime
            it[CDG_TRAFFIC_STATUS] = cdgStatus
            it[ORY_TRAVEL_TIME] = oryTime
            it[ORY_TRAFFIC_STATUS] = oryStatus
        }
    }

    suspend fun updateWeather(temp: String, desc: String, city: String) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[WEATHER_TEMP] = temp
            it[WEATHER_DESC] = desc
            it[WEATHER_CITY] = city
        }
    }

    suspend fun updateMetroData(json: String) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[METRO_DATA] = json
            it[LAST_PRIM_FETCH_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun updateFlights(json: String) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[UPCOMING_FLIGHTS] = json }
    }

    suspend fun setForceEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[IS_FORCE_ENABLED] = enabled }
    }

    suspend fun setWidgetFontSize(size: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[WIDGET_FONT_SIZE] = size }
    }

    suspend fun setWidgetTransparent(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[IS_WIDGET_TRANSPARENT] = enabled }
    }

    suspend fun setThresholds(cdg: Int, ory: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[CDG_THRESHOLD] = cdg
            it[ORY_THRESHOLD] = ory
        }
    }

    suspend fun updateLastNotified(time: Long) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[LAST_NOTIFIED_TIME] = time }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun setAppTheme(theme: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[APP_THEME] = theme }
    }

    suspend fun toggleLine(lineId: String) = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            val current = prefs[ENABLED_LINES]?.split(",")?.filter { it.isNotEmpty() }?.toMutableSet() ?: TrafficMapper.LINES_MAP.keys.toMutableSet()
            if (current.contains(lineId)) current.remove(lineId) else current.add(lineId)
            prefs[ENABLED_LINES] = current.joinToString(",")
        }
    }

    suspend fun setSmartRefresh(enabled: Boolean, distance: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[SMART_REFRESH_ENABLED] = enabled
            it[SMART_REFRESH_DISTANCE] = distance
        }
    }

    suspend fun setWorkingHours(start: Int, end: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[WORKING_START_HOUR] = start
            it[WORKING_END_HOUR] = end
        }
    }

    suspend fun setRefreshInterval(minutes: Int) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it[REFRESH_INTERVAL] = minutes
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            it.remove(METRO_DATA)
            it.remove(LAST_PRIM_FETCH_TIMESTAMP)
            it.remove(WEATHER_TEMP)
            it.remove(LAST_UPDATED)
            it.remove(CDG_MINUTES)
            it.remove(ORY_MINUTES)
            it.remove(CDG_TRAVEL_TIME)
            it.remove(ORY_TRAVEL_TIME)
            it.remove(IS_TAXI_DATA_STALE)
        }
    }

    suspend fun fullReset() = withContext(Dispatchers.IO) {
        context.dataStore.edit { it.clear() }
    }
}
