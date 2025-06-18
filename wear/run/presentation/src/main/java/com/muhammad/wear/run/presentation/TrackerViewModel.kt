package com.muhammad.wear.run.presentation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.core.connectivity.domain.messaging.MessagingAction
import com.muhammad.core.domain.util.Result
import com.muhammad.core.notification.ActiveRunService
import com.muhammad.wear.run.domain.ExerciseTracker
import com.muhammad.wear.run.domain.PhoneConnector
import com.muhammad.wear.run.domain.RunningTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TrackerViewModel(
    private val exerciseTracker: ExerciseTracker,
    private val phoneConnector: PhoneConnector,
    private val runningTracker: RunningTracker,
) : ViewModel() {
    private val _state = MutableStateFlow(
        TrackerState(
            hasStartedRunning = ActiveRunService.isServiceActive.value,
            isRunActive = ActiveRunService.isServiceActive.value && runningTracker.isTracking.value,
            isTracking = ActiveRunService.isServiceActive.value
        )
    )
    val state = _state.asStateFlow()
    private val hasBodySensorPermission = MutableStateFlow(false)
    private val isTracking = snapshotFlow {
        state.value.isRunActive && state.value.isTracking && state.value.isConnectedPhoneNearBy
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    private val _events = Channel<TrackerEvent>()
    val events = _events.receiveAsFlow()
    init {
        phoneConnector.connectedNode.filterNotNull().onEach { connectedNode ->
            _state.update { it.copy(isConnectedPhoneNearBy = connectedNode.isNearBy) }
        }.combine(isTracking) { _, isTracking ->
            if (!isTracking) {
                phoneConnector.sendActionToPhone(MessagingAction.ConnectionRequest)
            }
        }.launchIn(viewModelScope)
        runningTracker.isTrackable.onEach { isTrackable ->
            _state.update { it.copy(isTracking = isTrackable) }
        }.launchIn(viewModelScope)
        isTracking.onEach { isTracking ->
            val result = when {
                isTracking && !state.value.hasStartedRunning -> {
                    exerciseTracker.startExercise()
                }

                isTracking && state.value.hasStartedRunning -> {
                    exerciseTracker.resumeExercise()
                }

                !isTracking && state.value.hasStartedRunning -> {
                    exerciseTracker.pauseExercise()
                }

                else -> Result.Success(Unit)
            }
            if (result is Result.Failure) {
                result.error.toUiText()?.let { error ->
                    _events.send(TrackerEvent.Error(error))
                }
            }
            if (isTracking) {
                _state.update { it.copy(hasStartedRunning = true) }
            }
            runningTracker.setIsTracking(isTracking)
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            val isHeartRateTrackingSupported = exerciseTracker.isHeartRateTrackingSupported()
            _state.update { it.copy(canTrackHeartRate = isHeartRateTrackingSupported) }
        }
        val isAmbientMode = snapshotFlow { state.value.isAmbientMode }
        isAmbientMode.flatMapLatest { isAmbientMode ->
            if (isAmbientMode) {
                runningTracker.heartRate.sample(10.seconds)
            } else {
                runningTracker.heartRate
            }
        }.onEach { heartRate ->
            _state.update { it.copy(heartRate = heartRate) }
        }.launchIn(viewModelScope)
        isAmbientMode.flatMapLatest { isAmbientMode ->
            if (isAmbientMode) {
                runningTracker.elapsedTime.sample(10.seconds)
            } else {
                runningTracker.elapsedTime
            }
        }.onEach { time ->
            _state.update { it.copy(elapsedDuration = time) }
        }.launchIn(viewModelScope)
        runningTracker.distanceMeters.onEach { distanceMeters ->
            _state.update { it.copy(distanceMeters = distanceMeters) }
        }.launchIn(viewModelScope)
        listenToPhoneActions()
    }

    fun onAction(action: TrackerAction, triggerOnPhone: Boolean = false) {
        if (!triggerOnPhone) {
            sendActionToPhone(action)
        }
        when (action) {
            is TrackerAction.OnBodySensorPermissionResult -> {
                hasBodySensorPermission.value = action.isGranted
                if (action.isGranted) {
                    viewModelScope.launch {
                        val isHeartTrackingSupported =
                            exerciseTracker.isHeartRateTrackingSupported()
                        _state.update { it.copy(canTrackHeartRate = isHeartTrackingSupported) }
                    }
                }
            }

            is TrackerAction.OnEnterAmbientMode -> {
                _state.update {
                    it.copy(
                        isAmbientMode = true,
                        burnInProtectionRequired = action.burnInProtectionRequired
                    )
                }
            }

            TrackerAction.OnExitAmbientMode -> {
                _state.update { it.copy(isAmbientMode = false) }
            }

            TrackerAction.OnFinishRunClick -> {
                viewModelScope.launch {
                    exerciseTracker.stopExercise()
                    _events.send(TrackerEvent.RunFinished)
                    _state.update {
                        it.copy(
                            elapsedDuration = Duration.ZERO,
                            distanceMeters = 0,
                            heartRate = 0,
                            hasStartedRunning = false,
                            isRunActive = false
                        )
                    }
                }
            }

            TrackerAction.OnToggleRunClick -> {
                if (state.value.isTracking) {
                    _state.update { it.copy(isRunActive = !state.value.isRunActive) }
                }
            }
        }
    }

    private fun sendActionToPhone(action: TrackerAction) {
        viewModelScope.launch {
            val messagingAction = when (action) {
                TrackerAction.OnFinishRunClick -> MessagingAction.Finish
                TrackerAction.OnToggleRunClick -> {
                    if (state.value.isRunActive) MessagingAction.Pause else MessagingAction.StartOrResume
                }

                else -> null
            }
            messagingAction?.let { action ->
                val result = phoneConnector.sendActionToPhone(action)
                if(result is Result.Failure){
                    println("Tracker error: ${result.error}")
                }
            }
        }
    }

    private fun listenToPhoneActions() {
        phoneConnector.messagingActions.onEach { action ->
            when (action) {
                MessagingAction.Finish -> {
                    onAction(TrackerAction.OnFinishRunClick, triggerOnPhone = true)
                }

                MessagingAction.Pause -> {
                    if (state.value.isTracking) {
                        _state.update { it.copy(isRunActive = false) }
                    }
                }

                MessagingAction.StartOrResume -> {
                    _state.update { it.copy(isRunActive = true) }
                }

                MessagingAction.Trackable -> {
                    _state.update { it.copy(isTracking = true) }
                }

                MessagingAction.UnTrackable -> {
                    _state.update { it.copy(isTracking = false) }
                }

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }
}
