package com.taxiparis.app.data.repository

import com.taxiparis.app.data.remote.api.OsrmService
import com.taxiparis.app.domain.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrafficRepository @Inject constructor(
    private val osrmService: OsrmService
) {
    suspend fun fetchRoute(lon: Double, lat: Double, dLon: Double, dLat: Double): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val coords = String.format(java.util.Locale.US, "%.6f,%.6f;%.6f,%.6f", lon, lat, dLon, dLat)
            val response = osrmService.getRoute(coords)
            NetworkResult.Success(response.string())
        } catch (e: Exception) {
            NetworkResult.Error("Erreur réseau (OSRM) : ${e.localizedMessage}")
        }
    }
}
