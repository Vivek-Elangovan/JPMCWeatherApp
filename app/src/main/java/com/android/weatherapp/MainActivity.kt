@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.android.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.android.weatherapp.ui.theme.WeatherAppTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class MainActivity : ComponentActivity() {

    //location
    private val PERMISSION_ID = 13
    private lateinit var viewModel: MainViewModel

    //location shared SharedPreferences
    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            init()
            CurrentLocation(this).getLastLocation()
        }
        setContent {
            WeatherAppTheme {
                Column {
                    LocationSearch()
                    LiveDataComponent(
                        viewModel
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModelStore.put("state", viewModel)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel = viewModelStore["state"] as MainViewModel
    }

    /**
     * This function is used to initiate view model and request for the IP details
     */
    fun init() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        pref = getSharedPreferences("location", Context.MODE_PRIVATE)
        editor = pref?.edit()
    }

    /**
     * This function is used to check the device connectivity and return the result
     * @return isOnline Boolean
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
            }
        }
        return false
    }

    /**
     * This function will request for Location Permission
     */
    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                CurrentLocation(this).getLastLocation()
            }
        }
    }

    private fun fetchWeatherBasedOnCityName(cityName: String) {
        if (isOnline(this)) {
            viewModel.loading.value = true
            viewModel.getWeatherBasedOnCity(cityName)
        } else {
            Toast.makeText(
                this,
                getString(R.string.message_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * This function will fetch Current Location using the latitude and longitude from shared preference
     */
    fun fetchWeatherBasedOnCurrentLocation() {
        val latitude = pref?.getString("latitude", "").toString()
        val longitude = pref?.getString("longitude", "").toString()
        if (isOnline(this)) {
            if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                viewModel.loading.value = true
                viewModel.getWeatherBasedOnCurrentLocation(latitude, longitude)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.message_location_data_invalid),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.message_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Composable
    fun LocationSearch() {
        var value by remember {
            mutableStateOf("")
        }

        Row(modifier = Modifier.padding(8.dp, 16.dp, 8.dp, 16.dp)) {
            TextField(
                value = value,
                onValueChange = { newText ->
                    value = newText
                },
                modifier = Modifier.padding(0.dp, 0.dp, 16.dp, 0.dp),
                label = { Text(text = "City Name") },
                placeholder = { Text(text = "Type your City Name") }
            )
            Button(onClick = { fetchWeatherBasedOnCityName(value) }) {
                Text(text = "Search")
            }
        }
    }
}

@Composable
fun LiveDataComponent(viewModel: MainViewModel) {
    val weatherDetailState = viewModel.weatherDetailLiveData.observeAsState()
    val isLoading = viewModel.loading.observeAsState()
    if (isLoading.value == true) {
        LiveDataLoadingComponent()
    } else {
        weatherDetailState.value?.let { LiveDataComponentSetup(weatherDetail = it, viewModel) }
    }
}

/**
 *  We represent a Composable function by annotating it with the @Composable annotation. Composable
 *  functions can only be called from within the scope of other composable functions.
 */
@Composable
fun LiveDataLoadingComponent() {
    // Column is a composable that places its children in a vertical sequence. You
    // can think of it similar to a LinearLayout with the vertical orientation.
    // In addition we also pass a few modifiers to it.

    // You can think of Modifiers as implementations of the decorators pattern that are
    // used to modify the composable that its applied to. In this example, we configure the
    // Column composable to occupy the entire available width and height using
    // Modifier.fillMaxSize().
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // A pre-defined composable that's capable of rendering a circular progress indicator. It
        // honors the Material Design specification.
        CircularProgressIndicator(modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally))
    }
}

@Composable
fun LiveDataComponentSetup(weatherDetail: WeatherDetail, viewModel: MainViewModel) {

    // Card composable is a predefined composable that is meant to represent the
    // card surface as specified by the Material Design specification. We also
    // configure it to have rounded corners and apply a modifier.

    // You can think of Modifiers as implementations of the decorators pattern that are used to
    // modify the composable that its applied to. In this example, we assign a padding of
    // 8dp to the Card along with specifying it to occupy the entire available width.
    Card(
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // The Text composable is pre-defined by the Compose UI library; you can use this
        // composable to render text on the screen
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            Column {
                Text(
                    text = weatherDetail.name + ", " + weatherDetail.sys.country,
                    modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 4.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif, fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                )

                Text(
                    text = weatherDetail.weather[0].description,
                    modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 4.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif, fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                )

                Text(
                    text = "Temp : " + viewModel.convertTemp(weatherDetail.main.temp.toString()) + "\u2103" + "\nFeels Like : " + viewModel.convertTemp(
                        weatherDetail.main.feelsLike.toString()
                    ) + "\u2103" + "\nMax Temp : " + viewModel.convertTemp(weatherDetail.main.tempMax.toString()) + "\u2103" + "\nMin Temp : " + viewModel.convertTemp(
                        weatherDetail.main.tempMin.toString()
                    ) + "\u2103",
                    modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 16.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif, fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
            GlideImage(
                model = IconManager().getIcon(weatherDetail.weather[0].icon),
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .wrapContentWidth()
                    .height(150.dp)
            )
        }
    }
}