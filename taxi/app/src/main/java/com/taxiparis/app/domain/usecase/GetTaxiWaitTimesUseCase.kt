package com.taxiparis.app.domain.usecase

import com.taxiparis.app.domain.model.NetworkResult
import com.taxiparis.app.domain.model.TaxiWaitTimes
import com.taxiparis.app.data.repository.AirportRepository
import javax.inject.Inject

class GetTaxiWaitTimesUseCase @Inject constructor(
    private val airportRepository: AirportRepository
) {
    suspend operator fun invoke(): NetworkResult<TaxiWaitTimes> {
        return airportRepository.fetchTaxiWaitTimes()
    }
}
