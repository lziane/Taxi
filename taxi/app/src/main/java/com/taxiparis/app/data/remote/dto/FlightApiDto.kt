package com.taxiparis.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FlightApiResponse(
    val response: List<FlightApiItem>
)

@Serializable
data class FlightApiItem(
    val flight_number: String? = null,
    val flight_iata: String? = null,
    val airline_iata: String? = null,
    val dep_iata: String? = null,
    val arr_time: String? = null,
    val arr_terminal: String? = null,
    val aircraft_icao: String? = null,
    val status: String? = null
)
