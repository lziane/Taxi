package com.taxiparis.app.domain.usecase

import com.taxiparis.app.domain.model.NetworkResult
import com.taxiparis.app.data.repository.TransitRepository
import com.taxiparis.app.domain.model.LineStatus
import com.taxiparis.app.data.mapper.TrafficMapper
import javax.inject.Inject

class GetMetroTrafficUseCase @Inject constructor(
    private val transitRepository: TransitRepository
) {
    suspend operator fun invoke(): NetworkResult<List<LineStatus>> {
        return when (val result = transitRepository.fetchMetroDisruptions()) {
            is NetworkResult.Success -> {
                val lines = TrafficMapper.mapToLineStatus(result.data)
                NetworkResult.Success(lines)
            }
            is NetworkResult.Error -> result
        }
    }
}
