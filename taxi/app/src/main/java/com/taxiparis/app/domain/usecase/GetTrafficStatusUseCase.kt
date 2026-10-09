package com.taxiparis.app.domain.usecase

import com.taxiparis.app.domain.model.NetworkResult
import com.taxiparis.app.data.repository.TrafficRepository
import org.json.JSONObject
import javax.inject.Inject

data class TrafficInfo(
    val durationSeconds: Int,
    val distanceMeters: Int
)

class GetTrafficStatusUseCase @Inject constructor(
    private val trafficRepository: TrafficRepository
) {
    suspend operator fun invoke(lon: Double, lat: Double, dLon: Double, dLat: Double): NetworkResult<TrafficInfo> {
        return when (val result = trafficRepository.fetchRoute(lon, lat, dLon, dLat)) {
            is NetworkResult.Success -> {
                try {
                    val json = JSONObject(result.data)
                    val routes = json.optJSONArray("routes")
                    if (routes != null && routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        NetworkResult.Success(
                            TrafficInfo(
                                durationSeconds = route.getInt("duration"),
                                distanceMeters = route.getInt("distance")
                            )
                        )
                    } else {
                        NetworkResult.Error("Aucun itinéraire trouvé")
                    }
                } catch (e: Exception) {
                    NetworkResult.Error("Erreur de parsing OSRM")
                }
            }
            is NetworkResult.Error -> result
        }
    }
}
