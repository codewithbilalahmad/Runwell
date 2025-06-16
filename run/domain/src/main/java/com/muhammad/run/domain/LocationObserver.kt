package com.muhammad.run.domain

import com.muhammad.core.domain.location.LocationWithAltitude
import kotlinx.coroutines.flow.Flow

interface LocationObserver{
    fun observeLocation(interval : Long) : Flow<LocationWithAltitude>
}