package com.taxiparis.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrimResponse(
    @SerialName("disruptions")
    val disruptions: List<Disruption>? = null
)

@Serializable
data class Disruption(
    val id: String? = null,
    val title: String? = null,
    val message: String? = null,
    val severity: String? = null,
    @SerialName("impactedSections")
    val impactedSections: List<ImpactedSection>? = null,
    @SerialName("impactedObjects")
    val impactedObjects: List<ImpactedObject>? = null
)

@Serializable
data class ImpactedSection(
    @SerialName("lineId")
    val lineId: String? = null
)

@Serializable
data class ImpactedObject(
    val id: String? = null
)
