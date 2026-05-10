package com.masafyorg.weatherapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val api: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String): WeatherResponse =
        api.getCurrentWeather(lat, lon, apiKey)

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String): ForecastResponse =
        api.getForecast(lat, lon, apiKey)
}
