package com.taxiparis.app.data.repository

import com.taxiparis.app.data.remote.api.PrimService
import com.taxiparis.app.data.remote.dto.PrimResponse
import com.taxiparis.app.domain.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitRepository @Inject constructor(
    private val primService: PrimService
) {
    suspend fun fetchMetroDisruptions(): NetworkResult<PrimResponse> = withContext(Dispatchers.IO) {
        try {
            val response = primService.getDisruptions()
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error("Erreur réseau (PRIM) : ${e.localizedMessage}")
        }
    }
}
