package com.taxiparis.app.data.repository

import android.util.Log
import com.taxiparis.app.data.remote.api.FlightApiService
import com.taxiparis.app.domain.model.Flight
import com.taxiparis.app.domain.model.NetworkResult
import com.taxiparis.app.domain.model.TaxiWaitTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class AirportRepository @Inject constructor(
    private val flightApiService: FlightApiService
) {
    private val AIRLABS_KEY = "69438343-466d-495b-9860-23916d56d787" 

    suspend fun fetchFlights(airport: String): NetworkResult<List<Flight>> = withContext(Dispatchers.IO) {
        try {
            val response = flightApiService.getArrivals(AIRLABS_KEY, airport)
            val flights = response.response.filter { 
                it.flight_iata != null && it.status != "cancelled" 
            }.take(15).map { item ->
                val isHeavy = item.aircraft_icao in listOf("B77W", "A388", "A35K", "B789", "A333", "B77L", "B744")
                val score = if (isHeavy) 90 else 40
                
                Flight(
                    flightNo = item.flight_iata ?: "??",
                    airline = getAirlineName(item.airline_iata),
                    origin = item.dep_iata ?: "???",
                    arrivalTimeFrance = item.arr_time?.substringAfter("T")?.take(5) ?: "--:--",
                    departureTime = "--:--",
                    terminal = item.arr_terminal ?: "1",
                    airport = airport,
                    score = score,
                    passengers = if (isHeavy) 380 else 160,
                    aircraftType = item.aircraft_icao ?: "Inconnu",
                    importantInfo = if (isHeavy) "Long-Courrier (Gros porteur)" else "Court/Moyen courrier",
                    delay = if (item.status == "delayed") "Retardé" else null,
                    aircraftImageUrl = null
                )
            }
            NetworkResult.Success(flights.sortedBy { it.arrivalTimeFrance })
        } catch (e: Exception) {
            NetworkResult.Error("Erreur synchronisation vols : ${e.localizedMessage}")
        }
    }

    private fun getAirlineName(iata: String?): String = when(iata) {
        "AF" -> "Air France"
        "EK" -> "Emirates"
        "QR" -> "Qatar Airways"
        "DL" -> "Delta"
        "AA" -> "American Airlines"
        "LH" -> "Lufthansa"
        "TO" -> "Transavia"
        "U2" -> "EasyJet"
        else -> iata ?: "Compagnie"
    }

    suspend fun fetchTaxiWaitTimes(): NetworkResult<TaxiWaitTimes> = withContext(Dispatchers.IO) {
        val url = "https://infotaxi.parisaeroport.fr/"
        try {
            withTimeout(30000) {
                val response = Jsoup.connect(url)
                    .timeout(20000)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                    .execute()
                
                if (response.statusCode() != 200) {
                    return@withTimeout NetworkResult.Error("Source indisponible (HTTP ${response.statusCode()})")
                }
                
                val html = response.body()
                
                var cdgSec = extractSeconds(html, "setProgressCDG")
                var orySec = extractSeconds(html, "setProgressORY")

                if (cdgSec == null) cdgSec = findFallbackSeconds(html, "CDG")
                if (orySec == null) orySec = findFallbackSeconds(html, "ORY")
                
                if (cdgSec == null) cdgSec = deepSearchSeconds(html, "CDG")
                if (orySec == null) orySec = deepSearchSeconds(html, "ORY")

                if (cdgSec == null || orySec == null) {
                    Log.e("AirportRepository", "STRUCTURE_CHANGED: Scraping failed on $url")
                    return@withTimeout NetworkResult.Error("Structure du site modifiée")
                }

                NetworkResult.Success(
                    TaxiWaitTimes(
                        cdgMinutes = (cdgSec / 60.0).roundToInt().coerceAtLeast(0),
                        oryMinutes = (orySec / 60.0).roundToInt().coerceAtLeast(0)
                    )
                )
            }
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            NetworkResult.Error("Délai d'attente dépassé (30s)")
        } catch (_: java.net.UnknownHostException) {
            NetworkResult.Error("Pas de connexion internet")
        } catch (_: java.net.SocketTimeoutException) {
            NetworkResult.Error("Le serveur Infotaxi est trop lent à répondre")
        } catch (e: Exception) {
            Log.e("AirportRepository", "Unexpected scraping error", e)
            NetworkResult.Error("Erreur : ${e.localizedMessage}")
        }
    }

    private fun extractSeconds(html: String, functionName: String): Int? {
        val regex = Regex("""function\s+$functionName\s*\(\)\s*\{[^}]*var\s+seconds\s*=\s*(\d+)\s*;""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(html)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun findFallbackSeconds(html: String, keyword: String): Int? {
        val regex = Regex("""(?i)$keyword.*?var\s+seconds\s*=\s*(\d+)\s*;""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(html)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun deepSearchSeconds(html: String, keyword: String): Int? {
        val regex = Regex("""(?i)$keyword.{0,100}var\s+seconds\s*=\s*(\d+)""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(html)?.groupValues?.get(1)?.toIntOrNull()
    }
}
