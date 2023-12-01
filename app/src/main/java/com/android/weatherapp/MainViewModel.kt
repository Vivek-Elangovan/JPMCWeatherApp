package com.android.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    private val _weatherDetailLiveData = MutableLiveData<WeatherDetail>()
    val weatherDetailLiveData: LiveData<WeatherDetail>
        get() = _weatherDetailLiveData
    private var job: Job? = null
    val loading = MutableLiveData<Boolean>()

    /**
     * This function will initiate a job to request the current device IP details
     */
    fun getWeatherBasedOnCity(cityName: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler()).launch {
            val response =
                RetrofitBuilder.apiService.getWeatherBasedOnCity(cityName, RetrofitBuilder.API_KEY)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _weatherDetailLiveData.value = response.body()
                    loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    /**
     * This function will initiate a job to request the current device IP details
     */
    fun getWeatherBasedOnCurrentLocation(latitude: String, longitude: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler()).launch {
            val response =
                RetrofitBuilder.apiService.getWeatherBasedOnCurrentLocation(
                    latitude,
                    longitude,
                    RetrofitBuilder.API_KEY
                )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _weatherDetailLiveData.value = response.body()
                    loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    fun convertTemp(temperature: String): String {
        return ((((temperature).toFloat() - 273.15)).toInt()).toString()
    }

    /**
     * This function will set the error details
     * @param message
     */
    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    private fun exceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            onError("Exception handled: ${throwable.localizedMessage}")
        }
    }
}