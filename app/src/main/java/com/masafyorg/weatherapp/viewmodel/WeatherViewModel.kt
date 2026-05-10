package com.masafyorg.weatherapp.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.masafyorg.weatherapp.BuildConfig
import com.masafyorg.weatherapp.data.DailyForecast
import com.masafyorg.weatherapp.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = false,
    val locationName: String = "",
    val temperature: Double = 0.0,
    val weatherCode: Int = 800,
    val weatherDescription: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val forecast: List<DailyForecast> = emptyList(),
    val error: String? = null
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val prefs = application.getSharedPreferences("weather_memos", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _memos = MutableStateFlow<Map<String, String>>(emptyMap())
    val memos: StateFlow<Map<String, String>> = _memos.asStateFlow()

    init {
        _memos.value = prefs.all.mapValues { it.value as String }
    }

    fun saveMemo(dateKey: String, memo: String) {
        prefs.edit().putString(dateKey, memo).apply()
        _memos.value = _memos.value + (dateKey to memo)
    }

    @SuppressLint("MissingPermission")
    fun fetchWeather() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState(isLoading = true)
            try {
                val location = withTimeoutOrNull(10_000) {
                    fusedLocationClient.lastLocation.await()
                        ?: run {
                            val cancellationToken = CancellationTokenSource()
                            fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                                cancellationToken.token
                            ).await()
                        }
                }

                if (location == null) {
                    _uiState.value = WeatherUiState(error = "位置情報を取得できませんでした\n屋外で再度お試しください")
                    return@launch
                }

                val apiKey = BuildConfig.WEATHER_API_KEY
                val currentWeather = repository.getCurrentWeather(location.latitude, location.longitude, apiKey)
                val forecastResponse = repository.getForecast(location.latitude, location.longitude, apiKey)

                val today = LocalDate.now()
                val dailyForecasts = forecastResponse.list
                    .groupBy { item ->
                        Instant.ofEpochSecond(item.dt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .entries
                    .sortedBy { it.key }
                    .take(5)
                    .map { (date, items) ->
                        val dayLabel = when {
                            date == today -> "今日"
                            date == today.plusDays(1) -> "明日"
                            else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.JAPANESE)
                        }
                        DailyForecast(
                            dayLabel = dayLabel,
                            dateKey = date.toString(),
                            weatherCode = items.firstOrNull()?.weather?.firstOrNull()?.id ?: 800,
                            maxTemp = items.maxOf { it.main.temp },
                            minTemp = items.minOf { it.main.temp }
                        )
                    }

                _uiState.value = WeatherUiState(
                    isLoading = false,
                    locationName = currentWeather.name,
                    temperature = currentWeather.main.temp,
                    weatherCode = currentWeather.weather.firstOrNull()?.id ?: 800,
                    weatherDescription = currentWeather.weather.firstOrNull()?.description ?: "",
                    humidity = currentWeather.main.humidity,
                    windSpeed = currentWeather.wind.speed,
                    forecast = dailyForecasts
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState(error = "エラーが発生しました: ${e.message}")
            }
        }
    }
}
