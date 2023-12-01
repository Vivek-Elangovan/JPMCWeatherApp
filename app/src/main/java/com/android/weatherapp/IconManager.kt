package com.android.weatherapp

class IconManager {
    fun getIcon(weatherIcon: String?): Int {
        val icon: Int = when (weatherIcon) {
            "Sands" -> R.drawable.cloud_sunny
            "Clear" -> R.drawable.clear_night
            "Rain", "Snow", "Drizzle", "Thunderstorm" -> R.drawable.thunderstorm
            "Clouds" -> R.drawable.clouds_night
            "01d" -> R.drawable.sunny
            "01n" -> R.drawable.clear_night
            "09n", "09d", "10d", "10n" -> R.drawable.cloud_rain
            "11d", "11n" -> R.drawable.thunderstorm
            "02d", "02n", "03d", "03n", "04d", "04n" -> R.drawable.cloud_sunny
            else -> R.drawable.cloud_sunny
        }
        return icon
    }
}