package com.taxiparis.app.domain.usecase

import com.taxiparis.app.domain.model.Flight
import com.taxiparis.app.domain.model.NetworkResult
import com.taxiparis.app.data.repository.AirportRepository
import javax.inject.Inject

class GetFlightsUseCase @Inject constructor(
    private val airportRepository: AirportRepository
) {
    suspend operator fun invoke(airport: String): NetworkResult<List<Flight>> {
        return airportRepository.fetchFlights(airport)
    }
}
