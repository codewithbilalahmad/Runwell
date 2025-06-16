package com.muhammad.run.location

import android.location.Location
import com.muhammad.core.domain.location.LocationWithAltitude


fun Location.toLocationWithAltitude() : LocationWithAltitude{
    return LocationWithAltitude(
        location = com.muhammad.core.domain.location.Location(
            lat = latitude, long = longitude
        ), altitude = altitude
    )
}