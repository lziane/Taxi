package com.taxiparis.app.domain.usecase

import com.taxiparis.app.domain.model.NetworkResult
import com.taxiparis.app.data.repository.WeatherRepository
import com.taxiparis.app.data.remote.dto.WeatherResponse
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(): NetworkResult<WeatherResponse> {
        return weatherRepository.fetchWeather()
    }
}
