package com.taxiparis.app.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationTracker(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        // Points de repère précis des stations de taxi
        const val CDG_LAT = 49.0039 // Station taxi Terminal 2F (point central utile)
        const val CDG_LON = 2.5700
        const val ORY_LAT = 48.7295 // Station taxi Orly 4
        const val ORY_LON = 2.3675

        // Coordonnées centre Paris pour Geofencing IDF
        const val IDF_CENTER_LAT = 48.8566
        const val IDF_CENTER_LON = 2.3522
        const val IDF_RADIUS_KM = 60.0
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            // Priorité à lastLocation : extrêmement économe en batterie car n'active pas le GPS
            val last = client.lastLocation.await()
            
            // Si la dernière position est récente (< 2 min), on l'utilise directement
            if (last != null && (System.currentTimeMillis() - last.time) < 120_000) {
                return last
            }

            // Sinon, on demande une position équilibrée (Wifi/Réseau) plutôt que GPS pur
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

    fun calculateDistance(targetLat: Double, targetLon: Double, currentLoc: Location): Double {
        val results = FloatArray(1)
        Location.distanceBetween(currentLoc.latitude, currentLoc.longitude, targetLat, targetLon, results)
        return results[0].toDouble() / 1000.0 // KM
    }
}
