package com.taxiparis.app.data.mapper

import android.text.Html
import com.taxiparis.app.data.remote.dto.PrimResponse
import com.taxiparis.app.domain.model.LineStatus

object TrafficMapper {
    val LINES_MAP = mapOf(
        "line:IDFM:C01371" to "1", "line:IDFM:C01372" to "2", "line:IDFM:C01373" to "3",
        "line:IDFM:C01386" to "3b", "line:IDFM:C01374" to "4", "line:IDFM:C01375" to "5",
        "line:IDFM:C01376" to "6", "line:IDFM:C01377" to "7", "line:IDFM:C01387" to "7b",
        "line:IDFM:C01378" to "8", "line:IDFM:C01379" to "9", "line:IDFM:C01380" to "10",
        "line:IDFM:C01381" to "11", "line:IDFM:C01382" to "12", "line:IDFM:C01383" to "13",
        "line:IDFM:C01384" to "14", "line:IDFM:C01742" to "A", "line:IDFM:C01743" to "B",
        "line:IDFM:C01727" to "C", "line:IDFM:C01728" to "D", "line:IDFM:C01729" to "E"
    )

    fun mapToLineStatus(response: PrimResponse?): List<LineStatus> {
        val results = LINES_MAP.mapValues { (id, name) ->
            LineStatus(id, name, "Normal", "", "INFO", false)
        }.toMutableMap()

        response?.disruptions?.forEach { disruption ->
            val ids = (disruption.impactedSections?.mapNotNull { it.lineId } ?: emptyList()) +
                      (disruption.impactedObjects?.mapNotNull { it.id } ?: emptyList())

            ids.forEach { id ->
                if (LINES_MAP.containsKey(id)) {
                    val current = results[id]!!
                    val sev = disruption.severity ?: "INFO"

                    if (current.severity != "BLOQUANTE") {
                        val title = disruption.title ?: "Alerte"
                        results[id] = LineStatus(
                            lineId = id,
                            name = current.name,
                            status = title,
                            message = cleanText(disruption.message),
                            severity = sev,
                            isIncident = true
                        )
                    }
                }
            }
        }
        return LINES_MAP.keys.map { results[it]!! }
    }

    private fun cleanText(htmlText: String?): String {
        if (htmlText == null) return ""
        return try {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
                .toString()
                .replace(Regex("\\s+"), " ")
                .trim()
        } catch (e: Exception) { htmlText }
    }
}
