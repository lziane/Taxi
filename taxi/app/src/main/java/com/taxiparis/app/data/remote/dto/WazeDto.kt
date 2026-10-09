package com.taxiparis.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WazeResponse(
    val alternatives: List<WazeAlternative>? = null
)

@Serializable
data class WazeAlternative(
    val response: WazeRouteResponse? = null
)

@Serializable
data class WazeRouteResponse(
    val results: List<WazeResult>? = null
)

@Serializable
data class WazeResult(
    val length: Int, // mètres
    val crossTime: Int // secondes AVEC trafic
)
