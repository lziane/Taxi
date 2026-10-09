package com.taxiparis.app.data.remote.api

import com.taxiparis.app.data.remote.dto.FlightApiResponse
import com.taxiparis.app.data.remote.dto.PrimResponse
import com.taxiparis.app.data.remote.dto.WeatherResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PrimService {
    @GET("disruptions_bulk/disruptions/v2")
    suspend fun getDisruptions(): PrimResponse
}

interface WeatherService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double = 48.8566,
        @Query("longitude") lon: Double = 2.3522,
        @Query("current") current: String = "temperature_2m,weather_code"
    ): WeatherResponse
}

interface OsrmService {
    @GET("route/v1/driving/{coords}")
    suspend fun getRoute(
        @Path("coords") coords: String,
        @Query("overview") overview: String = "false"
    ): ResponseBody
}

interface FlightApiService {
    @GET("api/v9/schedules")
    suspend fun getArrivals(
        @Query("api_key") apiKey: String,
        @Query("arr_iata") airportCode: String // CDG or ORY
    ): FlightApiResponse
}

interface WazeService {
    @GET
    suspend fun getTraffic(
        @Url url: String,
        @Query(value = "from", encoded = true) from: String,
        @Query(value = "to", encoded = true) to: String,
        @Query("at") at: Int = 0,
        @Query("returnJSON") returnJSON: Boolean = true,
        @Query("nPaths") nPaths: Int = 1,
        @Query("options") options: String = "AVOID_TRAFFIC:n"
    ): ResponseBody
}
