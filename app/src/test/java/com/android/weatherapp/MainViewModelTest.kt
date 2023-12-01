package com.android.weatherapp

import com.nhaarman.mockitokotlin2.whenever
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import retrofit2.Response

@RunWith(JUnit4::class)
class MainViewModelTest : TestCase() {

    private lateinit var viewModel: MainViewModel

    @MockK
    private var ipDetails: WeatherDetail? = null

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = MainViewModel()
        ipDetails = mockIPDetails()
    }

    private fun mockIPDetails(): WeatherDetail? {
        return "{\"coord\":{\"lon\":-0.1257,\"lat\":51.5085},\"weather\":[{\"id\":804,\"main\":\"Clouds\",\"description\":\"overcast clouds\",\"icon\":\"04d\"}],\"base\":\"stations\",\"main\":{\"temp\":277.08,\"feels_like\":274.3,\"temp_min\":275.64,\"temp_max\":278.46,\"pressure\":1001,\"humidity\":80},\"visibility\":10000,\"wind\":{\"speed\":3.09,\"deg\":60},\"clouds\":{\"all\":93},\"dt\":1701351143,\"sys\":{\"type\":2,\"id\":268730,\"country\":\"GB\",\"sunrise\":1701330122,\"sunset\":1701359784},\"timezone\":0,\"id\":2643743,\"name\":\"London\",\"cod\":200}".toResponseBody() as WeatherDetail
    }

    @Test
    fun `should emit response object when api response success`() = runBlocking {
        val mockApiResponse = mockIPDetails()
        val apiService = Mockito.mock(ApiService::class.java)
        val mockResponse = Response.success(mockApiResponse)
        //Given
        whenever(apiService.getWeatherBasedOnCity("London", RetrofitBuilder.API_KEY)).thenReturn(
            mockResponse
        )
        //When
        viewModel.getWeatherBasedOnCity("London")
        viewModel.getWeatherBasedOnCurrentLocation("110", "120")

        //Then
        val result = viewModel.weatherDetailLiveData.getOrAwaitValue()
        assertEquals(mockApiResponse, result)
    }

    @Test
    fun fetchIPFromApi_positiveResponse() = runBlocking {
        // GIVEN
        val mockApiResponse = mockIPDetails()
        val apiService = Mockito.mock(ApiService::class.java)
        val mockResponse = Response.success(mockApiResponse)
        // WHEN
        Mockito.`when`(apiService.getWeatherBasedOnCity("London", RetrofitBuilder.API_KEY))
            .thenReturn(mockResponse)
        // THEN
        val weatherResponse = apiService.getWeatherBasedOnCity("London", RetrofitBuilder.API_KEY)
        val weather = weatherResponse.body()

    }

    @Test
    fun `getSchools - Given network failure then repository return Resource Failure`() {
        runBlocking {
            // GIVEN
            val apiService = Mockito.mock(ApiService::class.java)
            val mockResponse =
                Response.error<WeatherDetail>(500, "".toResponseBody())            // WHEN
            Mockito.`when`(apiService.getWeatherBasedOnCity("London", "API_KEY"))
                .thenReturn(mockResponse)
            // THEN
            val ipDetailsResponse = apiService.getWeatherBasedOnCity("London", "API_KEY")
            assertEquals(mockResponse.code(), ipDetailsResponse.code())
        }
    }
}