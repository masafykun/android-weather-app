package com.masafyorg.weatherapp.data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    val main: MainWeather,
    val weather: List<WeatherCondition>,
    val wind: Wind
)

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt: Long,
    val main: MainWeather,
    val weather: List<WeatherCondition>
)

data class MainWeather(
    val temp: Double,
    val humidity: Int,
    @SerializedName("temp_max") val tempMax: Double = 0.0,
    @SerializedName("temp_min") val tempMin: Double = 0.0
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class DailyForecast(
    val dayLabel: String,
    val dateKey: String,
    val weatherCode: Int,
    val maxTemp: Double,
    val minTemp: Double
)
