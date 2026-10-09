package com.taxiparis.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Flight(
    val flightNo: String,
    val airline: String,
    val origin: String,
    val arrivalTimeFrance: String,
    val departureTime: String,
    val terminal: String,
    val airport: String, // CDG or ORY
    val score: Int, // 0-100 (profitability)
    val passengers: Int,
    val delay: String? = null,
    val aircraftType: String? = null,
    val importantInfo: String? = null,
    val aircraftImageUrl: String? = null
)
