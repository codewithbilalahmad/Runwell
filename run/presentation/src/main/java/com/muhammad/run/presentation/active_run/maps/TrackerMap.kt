package com.muhammad.run.presentation.active_run.maps

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ktx.awaitSnapshot
import com.muhammad.core.domain.location.Location
import com.muhammad.core.domain.location.LocationTimeStamp
import com.muhammad.core.presentation.designsystem.RunIcon
import com.muhammad.run.presentation.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class, DelicateCoroutinesApi::class)
@Composable
fun TrackerMap(
    modifier: Modifier = Modifier,
    isRunFinished: Boolean,
    locations: List<List<LocationTimeStamp>>,
    currentLocation: Location?,
    onSnapshot: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val mapStyle = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberMarkerState()
    val markerPositionLat by animateFloatAsState(
        targetValue = currentLocation?.lat?.toFloat() ?: 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val markerPositionLong by animateFloatAsState(
        targetValue = currentLocation?.long?.toFloat() ?: 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val maskerPosition = remember(markerPositionLat, markerPositionLong) {
        LatLng(markerPositionLat.toDouble(), markerPositionLong.toDouble())
    }
    LaunchedEffect(maskerPosition, isRunFinished) {
        if(!isRunFinished){
            markerState.position = maskerPosition
        }
    }
    LaunchedEffect(currentLocation, isRunFinished) {
        if (currentLocation != null && !isRunFinished) {
            val latLng = LatLng(currentLocation.lat, currentLocation.long)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng,17f))
        }
    }
    var triggerCapture by remember { mutableStateOf(false) }
    var createSnapshotJob: Job? = remember { null }
    GoogleMap(
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = mapStyle),
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        modifier = if (isRunFinished) {
            modifier
                .width(300.dp)
                .aspectRatio(16 / 9f)
                .alpha(0f)
                .onSizeChanged { size ->
                    if (size.width >= 300) {
                        triggerCapture = true
                    }
                }
        } else modifier
    ) {
        RunwellPolylines(locations = locations)
        MapEffect(locations, isRunFinished, triggerCapture, createSnapshotJob) { map ->
            if (isRunFinished && triggerCapture && createSnapshotJob == null) {
                triggerCapture = true
                val boundsBuilder = LatLngBounds.builder()
                locations.flatten().onEach { location ->
                    boundsBuilder.include(
                        LatLng(
                            location.location.location.lat,
                            location.location.location.long
                        )
                    )
                }
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                map.setOnCameraIdleListener {
                    createSnapshotJob?.cancel()
                    createSnapshotJob = GlobalScope.launch {
                        delay(500L)
                        map.awaitSnapshot()?.let(onSnapshot)
                    }
                }
            }
        }
        if (!isRunFinished && currentLocation != null) {
            MarkerComposable(currentLocation, state = markerState) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RunIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}