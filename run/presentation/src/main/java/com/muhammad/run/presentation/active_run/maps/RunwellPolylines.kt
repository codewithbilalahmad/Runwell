package com.muhammad.run.presentation.active_run.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline
import com.muhammad.core.domain.location.LocationTimeStamp

@Composable
fun RunwellPolylines(locations : List<List<LocationTimeStamp>>) {
    val polylines = remember(locations) {
        locations.map { it.zipWithNext{timeStamp1, timeStamp2 ->
            PolylineUi(
                location1 = timeStamp1.location.location,
                location2 = timeStamp2.location.location,
                color = PolyColorCalculator.locationsToColors(
                    location1 = timeStamp1, location2 = timeStamp2
                )
            )
        } }
    }
    polylines.forEach {polyline ->
        polyline.forEach { polylineUi ->
            Polyline(
                points = listOf(
                    LatLng(polylineUi.location1.lat, polylineUi.location1.long),
                    LatLng(polylineUi.location2.lat, polylineUi.location2.long)
                ) ,color = polylineUi.color, jointType = JointType.BEVEL
            )
        }
    }
}