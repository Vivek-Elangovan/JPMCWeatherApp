package com.android.weatherapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class CurrentLocation(private val activity: Activity) {

    //fused location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null

    //SharedPreferences
    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        //check location permission
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        //check location permission
        pref = activity.getSharedPreferences("location", Context.MODE_PRIVATE)
        editor = pref?.edit()

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(activity) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        editor?.putString("latitude", latitude.toString())
                        editor?.putString("longitude", longitude.toString())
                        editor?.commit()
                        (activity as MainActivity?)?.fetchWeatherBasedOnCurrentLocation()
                    }
                }
            } else {
                Toast.makeText(activity, "Please turn on your location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                (activity as MainActivity?)?.startActivity(intent)
            }
        } else {
            (activity as MainActivity?)?.requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        //LocationManager reference
        locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager?

        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }

    //get location
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude

            editor?.putString("latitude", latitude.toString())
            editor?.putString("longitude", longitude.toString())
            editor?.commit()
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
}