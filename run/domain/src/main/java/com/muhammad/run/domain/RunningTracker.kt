package com.muhammad.run.domain

import com.muhammad.core.connectivity.domain.messaging.MessagingAction
import com.muhammad.core.domain.Timer
import com.muhammad.core.domain.location.LocationTimeStamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class RunningTracker(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
    private val watchConnector: WatchConnector,
) {
    private val _runData = MutableStateFlow(RunData())
    val runData = _runData.asStateFlow()
    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()
    private val isObservingLocation = MutableStateFlow(false)
    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()
    val currentLocation = isObservingLocation.flatMapLatest { isObservingLocation ->
        if (isObservingLocation) {
            println("Observing Location...")
            locationObserver.observeLocation(1000L)
        } else flowOf()
    }.stateIn(applicationScope, SharingStarted.Lazily, null)
    val heartRates = isTracking.flatMapLatest { isTracking ->
        if (isTracking) {
            watchConnector.messagingActions
        } else flowOf()
    }.filterIsInstance<MessagingAction.HeartRateUpdate>().map { it.heartRate }
        .runningFold(initial = emptyList<Int>()) { currentHeartRates, newHeatRates ->
            currentHeartRates + newHeatRates
        }.stateIn(applicationScope, SharingStarted.Lazily, emptyList())

    init {
        _isTracking.onEach { isTracking ->
            if (!isTracking) {
                val newList = buildList {
                    addAll(runData.value.locations)
                    add(emptyList<LocationTimeStamp>())
                }.toList()
                _runData.update { it.copy(locations = newList) }
            }
        }.flatMapLatest { isTracking ->
            if (isTracking) {
                Timer.timeAndEmit()
            } else flowOf()
        }.onEach { time ->
            _elapsedTime.value += time
        }.launchIn(applicationScope)
        currentLocation.filterNotNull().combineTransform(_isTracking) { location, isTracking ->
            if (isTracking) {
                emit(location)
            }
        }.zip(_elapsedTime) { location, elapsedTime ->
            LocationTimeStamp(location, elapsedTime)
        }.combine(heartRates) { location, heartRates ->
            val currentLocations = runData.value.locations
            val lastLocationsList = if (currentLocations.isNotEmpty()) {
                currentLocations.last() + location
            } else listOf(location)
            val newLocationList = currentLocations.replaceLast(lastLocationsList)
            val distanceMeters = LocationDataCalculator.getTotalDistanceMeters(locations = newLocationList)
            val distanceKm = distanceMeters / 1000.0
            val currentDuration = location.durationTimeStamp
            val avgSecondsPerKm =
                if (distanceKm == 0.0) 0 else (currentDuration.inWholeSeconds / distanceKm).roundToInt()
            _runData.update {
                it.copy(
                    distanceMeters = distanceMeters,
                    locations = newLocationList,
                    heartRates = heartRates, pace = avgSecondsPerKm.seconds
                )
            }
        }.launchIn(applicationScope)
        elapsedTime.onEach { time ->
            watchConnector.sendActionToWatch(MessagingAction.TimeUpdate(time))
        }.launchIn(applicationScope)
        runData.map { it.distanceMeters }.distinctUntilChanged().onEach {distance ->
            watchConnector.sendActionToWatch(
                MessagingAction.DistanceUpdate(distance)
            )
        }.launchIn(applicationScope)
    }
    fun setIsTracking(isTracking : Boolean){
        _isTracking.update { isTracking }
    }
    fun startObservingLocation(){
        isObservingLocation.update { true }
        watchConnector.setIsTrackable(true)
    }
    fun stopObservingLocation(){
        isObservingLocation.update { false }
        watchConnector.setIsTrackable(false)
    }
    fun finishRun(){
        stopObservingLocation()
        setIsTracking(false)
        _elapsedTime.value = Duration.ZERO
        _runData.value = RunData()
    }
}
private fun <T> List<List<T>>.replaceLast(replacement : List<T>) : List<List<T>>{
    if(this.isEmpty()){
        return listOf(replacement)
    }
    return this.dropLast(1) + listOf(replacement)
}