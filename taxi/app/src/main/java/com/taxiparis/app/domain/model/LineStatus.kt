package com.taxiparis.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LineStatus(
    val lineId: String,
    val name: String,
    val status: String,
    val message: String,
    val severity: String,
    val isIncident: Boolean
)

data class TrafficInfo(
    val status: String,
    val message: String,
    val sev: String,
    val isIncident: Boolean
)
