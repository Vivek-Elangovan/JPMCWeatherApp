package com.android.weatherapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("weather")
    suspend fun getWeatherBasedOnCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String
    ): Response<WeatherDetail>

    @GET("weather")
    suspend fun getWeatherBasedOnCurrentLocation(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") apiKey: String
    ): Response<WeatherDetail>
}