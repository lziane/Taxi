package com.taxiparis.app.domain.usecase

import android.util.Log
import com.taxiparis.app.common.LocationTracker
import com.taxiparis.app.data.local.TaxiPrefs
import com.taxiparis.app.domain.model.*
import com.taxiparis.app.notification.NotificationManager
import com.taxiparis.app.ui.util.formatMinutes
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncDataUseCase @Inject constructor(
    private val prefs: TaxiPrefs,
    private val getTaxiWaitTimes: GetTaxiWaitTimesUseCase,
    private val getFlights: GetFlightsUseCase,
    private val getMetroTraffic: GetMetroTrafficUseCase,
    private val getWeather: GetWeatherUseCase,
    private val getTrafficStatus: GetTrafficStatusUseCase,
    private val locationTracker: LocationTracker,
    private val notificationManager: NotificationManager
) {
    suspend operator fun invoke(forceMetro: Boolean = false) = coroutineScope {
        val state = prefs.taxiState.first()
        val now = System.currentTimeMillis()

        prefs.setRefreshing(true)
        if (forceMetro) prefs.setMetroRefreshing(true)

        // 1. Position & Trafic
        val currentLocation = locationTracker.getCurrentLocation()
        var dCdg = state.cdgDistance
        var dOry = state.oryDistance
        var isIDF = true

        val shouldUpdateLocation = (now - state.lastUpdated >= 5 * 60 * 1000L) || forceMetro || state.isForceEnabled

        if (currentLocation != null && shouldUpdateLocation) {
            dCdg = locationTracker.calculateDistance(LocationTracker.CDG_LAT, LocationTracker.CDG_LON, currentLocation)
            dOry = locationTracker.calculateDistance(LocationTracker.ORY_LAT, LocationTracker.ORY_LON, currentLocation)
            
            if (Math.abs(dCdg - state.cdgDistance) > 0.5 || Math.abs(dOry - state.oryDistance) > 0.5) {
                prefs.updateDistances(dCdg, dOry)
            }

            val distFromCenter = locationTracker.calculateDistance(LocationTracker.IDF_CENTER_LAT, LocationTracker.IDF_CENTER_LON, currentLocation)
            isIDF = distFromCenter <= LocationTracker.IDF_RADIUS_KM

            // Lancement parallèle du trafic
            val cdgTrafficDeferred = async { getTrafficStatus(currentLocation.longitude, currentLocation.latitude, LocationTracker.CDG_LON, LocationTracker.CDG_LAT) }
            val oryTrafficDeferred = async { getTrafficStatus(currentLocation.longitude, currentLocation.latitude, LocationTracker.ORY_LON, LocationTracker.ORY_LAT) }

            val cdgTraffic = cdgTrafficDeferred.await()
            val oryTraffic = oryTrafficDeferred.await()

            prefs.updateTrafficInfo(
                cdgTime = if (cdgTraffic is NetworkResult.Success) "${cdgTraffic.data.durationSeconds / 60} min" else "--",
                cdgStatus = if (cdgTraffic is NetworkResult.Success) getTrafficStatusLabel(cdgTraffic.data.durationSeconds, cdgTraffic.data.distanceMeters) else 0,
                oryTime = if (oryTraffic is NetworkResult.Success) "${oryTraffic.data.durationSeconds / 60} min" else "--",
                oryStatus = if (oryTraffic is NetworkResult.Success) getTrafficStatusLabel(oryTraffic.data.durationSeconds, oryTraffic.data.distanceMeters) else 0
            )
        }

        // 2. Taxis, Métro et Météo en parallèle
        val minDistance = minOf(dCdg, dOry)
        val isFar = (state.smartRefreshEnabled && minDistance > state.smartRefreshDistance) || !isIDF
        val threshold = if (isFar) 60 * 60 * 1000L else 10 * 60 * 1000L
        
        val shouldUpdateTaxi = (now - state.lastUpdated >= threshold) || state.isForceEnabled || forceMetro
        val shouldUpdateMetro = (now - state.lastPrimFetchTimestamp >= 45 * 60 * 1000L) || state.isForceEnabled || forceMetro
        val shouldUpdateWeather = (now - state.lastUpdated >= 60 * 60 * 1000L)

        val taxiDeferred = if (shouldUpdateTaxi) async { getTaxiWaitTimes() } else null
        val cdgFlightsDeferred = if (shouldUpdateTaxi) async { getFlights("CDG") } else null
        val oryFlightsDeferred = if (shouldUpdateTaxi) async { getFlights("ORY") } else null
        val metroDeferred = if (shouldUpdateMetro) async { getMetroTraffic() } else null
        val weatherDeferred = if (shouldUpdateWeather) async { getWeather() } else null

        // Traitement Taxi
        taxiDeferred?.await()?.let { taxiResult ->
            when (taxiResult) {
                is NetworkResult.Success -> {
                    val data = taxiResult.data
                    prefs.updateWaitTimes(data.cdgMinutes, data.oryMinutes, isStale = false)
                    checkAndNotify(data, state)
                }
                is NetworkResult.Error -> {
                    prefs.updateWaitTimes(state.cdgMinutes, state.oryMinutes, isStale = true)
                }
            }
        }

        // Traitement Vols
        val allFlights = mutableListOf<Flight>()
        cdgFlightsDeferred?.await()?.let { if (it is NetworkResult.Success) allFlights.addAll(it.data) }
        oryFlightsDeferred?.await()?.let { if (it is NetworkResult.Success) allFlights.addAll(it.data) }
        if (allFlights.isNotEmpty()) {
            prefs.updateFlights(Json.encodeToString(allFlights))
        }

        // Traitement Métro
        metroDeferred?.await()?.let { metroResult ->
            if (metroResult is NetworkResult.Success) {
                val json = Json.encodeToString(metroResult.data)
                prefs.updateMetroData(json)
            }
        }

        // Traitement Météo
        weatherDeferred?.await()?.let { weatherResult ->
            if (weatherResult is NetworkResult.Success) {
                prefs.updateWeather("${weatherResult.data.current.temperature.toInt()}°C", "Paris", "Paris")
            }
        }

        prefs.setRefreshing(false)
        prefs.setMetroRefreshing(false)
    }

    private fun getTrafficStatusLabel(seconds: Int, meters: Int): Int {
        if (seconds <= 0 || meters <= 0) return 0
        val speedKmh = (meters.toDouble() / 1000.0) / (seconds.toDouble() / 3600.0)
        return when {
            speedKmh < 35 -> 2 
            speedKmh < 65 -> 1 
            else -> 0          
        }
    }

    private suspend fun checkAndNotify(data: TaxiWaitTimes, state: TaxiState) {
        val now = System.currentTimeMillis()
        
        if (state.cdgThreshold > 0 && data.cdgMinutes <= state.cdgThreshold) {
            val vibrate = (now > state.lastNotifiedTime) && state.vibrationEnabled
            notificationManager.showAlert("CDG", formatMinutes(data.cdgMinutes), data.cdgMinutes, vibrate)
        } else {
            notificationManager.dismissAlert("CDG")
        }

        if (state.oryThreshold > 0 && data.oryMinutes <= state.oryThreshold) {
            val vibrate = (now > state.lastNotifiedTime) && state.vibrationEnabled
            notificationManager.showAlert("Orly", formatMinutes(data.oryMinutes), data.oryMinutes, vibrate)
        } else {
            notificationManager.dismissAlert("Orly")
        }
        
        if (now > state.lastNotifiedTime && (
            (state.cdgThreshold > 0 && data.cdgMinutes <= state.cdgThreshold) || 
            (state.oryThreshold > 0 && data.oryMinutes <= state.oryThreshold)
        )) {
            prefs.updateLastNotified(now + 30 * 60 * 1000L)
        }
    }
}
