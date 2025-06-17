package com.muhammad.run.presentation.active_run

import android.annotation.SuppressLint
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.core.connectivity.domain.messaging.MessagingAction
import com.muhammad.core.domain.location.Location
import com.muhammad.core.domain.run.Run
import com.muhammad.core.domain.run.RunRepository
import com.muhammad.core.domain.util.Result
import com.muhammad.core.notification.ActiveRunService
import com.muhammad.core.presentation.ui.asUiText
import com.muhammad.run.domain.LocationDataCalculator
import com.muhammad.run.domain.RunningTracker
import com.muhammad.run.domain.WatchConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
class ActiveRunViewModel constructor(
    private val runningTracker: RunningTracker,
    private val runRepository: RunRepository,
    private val watchConnector: WatchConnector,
    private val applicationScope: CoroutineScope,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ActiveRunState(
            shouldTrack = ActiveRunService.isServiceActive.value && runningTracker.isTracking.value,
            hasStartingRunning = ActiveRunService.isServiceActive.value
        )
    )
    val state = _state.asStateFlow()
    private val _events = Channel<ActiveRunEvent>()
    val events = _events.receiveAsFlow()
    private val shouldTrack = snapshotFlow { state.value.shouldTrack }.stateIn(
        viewModelScope,
        SharingStarted.Lazily, state.value.shouldTrack
    )
    private val hasLocationPermission = MutableStateFlow(false)
    private val isTracking =
        combine(shouldTrack, hasLocationPermission) { shouldTrack, hasLocationPermission ->
            shouldTrack && hasLocationPermission
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        hasLocationPermission.onEach { hasPermission ->
            if (hasPermission) {
                runningTracker.startObservingLocation()
            } else {
                runningTracker.stopObservingLocation()
            }
        }.launchIn(viewModelScope)
        isTracking.onEach { isTracking ->
            runningTracker.setIsTracking(isTracking)
        }.launchIn(viewModelScope)
        runningTracker.currentLocation.onEach { state ->
            _state.value = _state.value.copy(currentLocation = state?.location)
        }.launchIn(viewModelScope)
        runningTracker.runData.onEach { data ->
            _state.update { it.copy(runData = data) }
        }.launchIn(viewModelScope)
        runningTracker.elapsedTime.onEach { elapsedTime ->
            _state.update { it.copy(elapsedTime = elapsedTime) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ActiveRunAction, triggerOnWatch: Boolean = true) {
        if (!triggerOnWatch) {
            val messagingAction = when (action) {
                ActiveRunAction.OnFinishRunClick -> MessagingAction.Finish
                ActiveRunAction.OnResumeRunClick -> MessagingAction.StartOrResume
                ActiveRunAction.OnToggleRunClick -> {
                    if (state.value.hasStartingRunning) {
                        MessagingAction.Pause
                    } else {
                        MessagingAction.StartOrResume
                    }
                }

                else -> null
            }
            messagingAction?.let { action ->
                viewModelScope.launch {
                    watchConnector.sendActionToWatch(action)
                }
            }
        }
        when (action) {
            ActiveRunAction.DismissRationaleDialog -> {
                _state.update {
                    it.copy(showNotificationRationale = false, showLocationRationale = false)
                }
            }

            ActiveRunAction.OnBackClick -> {
                _state.update { it.copy(shouldTrack = false) }
            }

            ActiveRunAction.OnFinishRunClick -> {
                _state.update { it.copy(isRunFinished = true, isSavingRun = true) }
            }

            ActiveRunAction.OnResumeRunClick -> {
                _state.update { it.copy(shouldTrack = true) }
            }

            is ActiveRunAction.OnRunProcessed -> {
                finishRun(action.mapPictureByteArray)
            }

            ActiveRunAction.OnToggleRunClick -> {
                _state.update {
                    it.copy(
                        hasStartingRunning = true,
                        shouldTrack = !state.value.shouldTrack
                    )
                }
            }

            is ActiveRunAction.SubmitLocationPermissionInfo -> {
                hasLocationPermission.value = action.acceptedLocationPermission
                _state.update { it.copy(showLocationRationale = action.showLocationRationale) }
            }

            is ActiveRunAction.SubmitNotificationPermissionInfo -> {
                _state.update { it.copy(showNotificationRationale = action.showNotificationRationale) }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun finishRun(mapPictureByteArray: ByteArray) {
        val locations = state.value.runData.locations
        if (locations.isEmpty() || locations.first().size <= 1) {
            _state.update { it.copy(isSavingRun = false) }
            return
        }
        viewModelScope.launch {
            val run = Run(
                id = null,
                duration = state.value.elapsedTime,
                dateTimeUTC = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")),
                distanceMeters = state.value.runData.distanceMeters,
                location = state.value.currentLocation ?: Location(0.0, 0.0),
                maxSpeedKmh = LocationDataCalculator.getMaxSpeedKmh(locations),
                totalElevationMeters = LocationDataCalculator.getTotalElevatedMeters(locations),
                mapPictureUrl = null,
                maxHeartRate = if (state.value.runData.heartRates.isNotEmpty()) state.value.runData.heartRates.max() else null,
                avgHeartRate = if (state.value.runData.heartRates.isNotEmpty()) state.value.runData.heartRates.average()
                    .roundToInt() else null,
            )
            runningTracker.finishRun()
            when (val result = runRepository.upsertRun(run, mapPictureByteArray)) {
                is Result.Failure -> {
                    _events.send(ActiveRunEvent.Error(result.error.asUiText()))
                }

                is Result.Success -> {
                    _events.send(ActiveRunEvent.RunSaved)
                }
            }
            _state.update { it.copy(isSavingRun = false) }
        }
    }

    private fun listenToWatchActions() {
        watchConnector.messagingActions.onEach { action ->
            when (action) {
                MessagingAction.ConnectionRequest -> {
                    if (isTracking.value) {
                        watchConnector.sendActionToWatch(MessagingAction.StartOrResume)
                    }
                }

                MessagingAction.Finish -> {
                    onAction(action = ActiveRunAction.OnFinishRunClick, triggerOnWatch = true)
                }

                MessagingAction.Pause -> {
                    onAction(action = ActiveRunAction.OnToggleRunClick, triggerOnWatch = true)
                }

                MessagingAction.StartOrResume -> {
                    if (!isTracking.value) {
                        if (state.value.hasStartingRunning) {
                            onAction(
                                action = ActiveRunAction.OnResumeRunClick,
                                triggerOnWatch = true
                            )
                        } else {
                            onAction(
                                action = ActiveRunAction.OnToggleRunClick,
                                triggerOnWatch = true
                            )
                        }
                    }
                }

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveRunService.isServiceActive.value) {
            applicationScope.launch {
                watchConnector.sendActionToWatch(MessagingAction.UnTrackable)
            }
            runningTracker.stopObservingLocation()
        }
    }
}