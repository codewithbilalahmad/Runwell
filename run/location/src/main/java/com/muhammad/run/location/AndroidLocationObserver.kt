package com.muhammad.run.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.muhammad.core.domain.location.LocationWithAltitude
import com.muhammad.run.domain.LocationObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationObserver(
    private val context: Context,
) : LocationObserver {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    override fun observeLocation(interval: Long): Flow<LocationWithAltitude> {
        return callbackFlow {
            val locationManager = context.getSystemService(LocationManager::class.java)
            var isGpsEnabled = false
            var isNetworkEnabled = false
            while (!isGpsEnabled && !isNetworkEnabled) {
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (!isGpsEnabled && !isNetworkEnabled) {
                    delay(3000L)
                }
            }
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED){
                println("Location Permissions not granted!")
                close()
            } else{
                println("Getting Location..")
                client.lastLocation.addOnSuccessListener { location ->
                    location?.let { loc ->
                        println("Current Location -> Lat : ${loc.latitude} , Long : ${loc.longitude}")
                        trySend(location.toLocationWithAltitude())
                    }
                }
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).build()
                val locationCallBack = object : LocationCallback(){
                    override fun onLocationResult(result: LocationResult) {
                        super.onLocationResult(result)
                        result.locations.lastOrNull()?.let { location ->
                            println("Current Location -> Lat : ${location.latitude} , Long : ${location.longitude}")
                            trySend(location.toLocationWithAltitude())
                        }
                    }
                }
                client.requestLocationUpdates(request, locationCallBack, Looper.getMainLooper())
                awaitClose{
                    client.removeLocationUpdates(locationCallBack)
                }
            }
        }
    }
}