package com.masafyorg.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masafyorg.weatherapp.data.DailyForecast
import com.masafyorg.weatherapp.viewmodel.WeatherUiState
import com.masafyorg.weatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel = viewModel()) {
    val uiState by weatherViewModel.uiState.collectAsState()
    val memos by weatherViewModel.memos.collectAsState()
    val context = LocalContext.current

    var selectedForecast by remember { mutableStateOf<DailyForecast?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) weatherViewModel.fetchWeather()
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            weatherViewModel.fetchWeather()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = weatherGradient(uiState.weatherCode)),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Color.White)
            uiState.error != null -> Text(
                text = uiState.error!!,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            else -> WeatherContent(
                uiState = uiState,
                memos = memos,
                onForecastClick = { selectedForecast = it }
            )
        }
    }

    selectedForecast?.let { forecast ->
        MemoDialog(
            forecast = forecast,
            currentMemo = memos[forecast.dateKey] ?: "",
            onDismiss = { selectedForecast = null },
            onSave = { memo ->
                weatherViewModel.saveMemo(forecast.dateKey, memo)
                selectedForecast = null
            }
        )
    }
}

@Composable
private fun MemoDialog(
    forecast: DailyForecast,
    currentMemo: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentMemo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${forecast.dayLabel}のメモ",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("予定を入力...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun WeatherContent(
    uiState: WeatherUiState,
    memos: Map<String, String>,
    onForecastClick: (DailyForecast) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "📍 ${uiState.locationName}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = weatherEmoji(uiState.weatherCode),
            fontSize = 100.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${uiState.temperature.toInt()}°C",
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Thin
        )

        Text(
            text = uiState.weatherDescription,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubInfoItem(label = "💧 湿度", value = "${uiState.humidity}%")
            Spacer(modifier = Modifier.width(48.dp))
            SubInfoItem(label = "💨 風速", value = "${uiState.windSpeed}m/s")
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(uiState.forecast) { day ->
                ForecastDayItem(
                    forecast = day,
                    memo = memos[day.dateKey] ?: "",
                    onClick = { onForecastClick(day) }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SubInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ForecastDayItem(
    forecast: DailyForecast,
    memo: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .background(
                color = Color.White.copy(alpha = if (memo.isNotEmpty()) 0.2f else 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .width(56.dp)
    ) {
        Text(text = forecast.dayLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = weatherEmoji(forecast.weatherCode), fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "${forecast.maxTemp.toInt()}°", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(text = "${forecast.minTemp.toInt()}°", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        if (memo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = memo,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "＋", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        }
    }
}

private fun weatherEmoji(code: Int): String = when {
    code in 200..299 -> "⛈️"
    code in 300..399 -> "🌦️"
    code in 500..599 -> "🌧️"
    code in 600..699 -> "❄️"
    code in 700..799 -> "🌫️"
    code == 800 -> "☀️"
    code == 801 -> "🌤️"
    code == 802 -> "⛅"
    code in 803..804 -> "☁️"
    else -> "🌈"
}

@Composable
private fun weatherGradient(code: Int): Brush = when {
    code in 200..299 -> Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF4A0E8F)))
    code in 300..599 -> Brush.verticalGradient(listOf(Color(0xFF4A4A5A), Color(0xFF1C2B4A)))
    code in 600..699 -> Brush.verticalGradient(listOf(Color(0xFFE8F4FD), Color(0xFF89CFF0)))
    code in 700..799 -> Brush.verticalGradient(listOf(Color(0xFFB0BEC5), Color(0xFF78909C)))
    code == 800 -> Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFFFF8F00)))
    code in 801..804 -> Brush.verticalGradient(listOf(Color(0xFF5B7DB1), Color(0xFF8EA8C3)))
    else -> Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))
}
