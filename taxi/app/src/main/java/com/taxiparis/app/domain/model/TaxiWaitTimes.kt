package com.taxiparis.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaxiWaitTimes(
    val cdgMinutes: Int,
    val oryMinutes: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
