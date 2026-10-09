package com.taxiparis.app.di

import com.taxiparis.app.BuildConfig
import com.taxiparis.app.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                val host = chain.request().url.host
                val primApiKey = BuildConfig.PRIM_API_KEY
                
                request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                request.addHeader("Accept", "application/json, text/plain, */*")
                
                if (host.contains("prim.iledefrance-mobilites.fr")) {
                    request.addHeader("apikey", primApiKey)
                }
                
                chain.proceed(request.build())
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("PrimRetrofit")
    fun providePrimRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://prim.iledefrance-mobilites.fr/marketplace/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("WeatherRetrofit")
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("OsrmRetrofit")
    fun provideOsrmRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://router.project-osrm.org/")
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    @Named("AirLabsRetrofit")
    fun provideAirLabsRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://airlabs.co/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun providePrimService(@Named("PrimRetrofit") retrofit: Retrofit): PrimService =
        retrofit.create(PrimService::class.java)

    @Provides
    @Singleton
    fun provideWeatherService(@Named("WeatherRetrofit") retrofit: Retrofit): WeatherService =
        retrofit.create(WeatherService::class.java)

    @Provides
    @Singleton
    fun provideOsrmService(@Named("OsrmRetrofit") retrofit: Retrofit): OsrmService =
        retrofit.create(OsrmService::class.java)

    @Provides
    @Singleton
    fun provideFlightApiService(@Named("AirLabsRetrofit") retrofit: Retrofit): FlightApiService =
        retrofit.create(FlightApiService::class.java)
}
