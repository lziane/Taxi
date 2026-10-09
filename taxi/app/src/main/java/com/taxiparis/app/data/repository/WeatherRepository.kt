package com.taxiparis.app.data.repository

import com.taxiparis.app.data.remote.api.WeatherService
import com.taxiparis.app.data.remote.dto.WeatherResponse
import com.taxiparis.app.domain.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService
) {
    suspend fun fetchWeather(): NetworkResult<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            val response = weatherService.getWeather()
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error("Erreur réseau (Weather) : ${e.localizedMessage}")
        }
    }
}
