package com.muhammad.wear.run.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.Text
import com.muhammad.core.notification.ActiveRunService
import com.muhammad.core.presentation.designsystem.ExclamationMarkIcon
import com.muhammad.core.presentation.designsystem.FinishIcon
import com.muhammad.core.presentation.designsystem.PauseIcon
import com.muhammad.core.presentation.designsystem.StartIcon
import com.muhammad.core.presentation.ui.ObserveAsEvents
import com.muhammad.core.presentation.ui.formatted
import com.muhammad.core.presentation.ui.toFormattedHeartRate
import com.muhammad.core.presentation.ui.toFormattedKm
import com.muhammad.wear.run.presentation.ambinet.AmbientObserver
import com.muhammad.wear.run.presentation.ambinet.ambientMode
import com.muhammad.wear.run.presentation.components.RunDataCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackerScreen(
    onServiceToggle: (Boolean) -> Unit,
    viewModel: TrackerViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isServiceActive by ActiveRunService.isServiceActive.collectAsStateWithLifecycle()
    LaunchedEffect(state.isRunActive,state.hasStartedRunning, isServiceActive) {
        if(state.isRunActive && !isServiceActive){
            onServiceToggle(true)
        }
    }
    ObserveAsEvents(viewModel.events) {event ->
        when(event){
            is TrackerEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            TrackerEvent.RunFinished ->{
                onServiceToggle(false)
            }
        }
    }
    TrackerScreenContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun TrackerScreenContent(state: TrackerState, onAction: (TrackerAction) -> Unit) {
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val hasOnBodySensorPermission = perms[Manifest.permission.BODY_SENSORS] == true
            onAction(TrackerAction.OnBodySensorPermissionResult(hasOnBodySensorPermission))
        }
    val context = LocalContext.current
    LaunchedEffect(true) {
        val hasBodySensorPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
        val hasNotificationNotification = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        val permissions = mutableListOf<String>()
        if (!hasBodySensorPermission) {
            permissions.add(Manifest.permission.BODY_SENSORS)
        }
        if (!hasNotificationNotification) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
    AmbientObserver(onEnterAmbient = {
        onAction(TrackerAction.OnEnterAmbientMode(it.burnInProtectionRequired))
    }, onExitAmbient = {
        onAction(TrackerAction.OnExitAmbientMode)
    })
    if (state.isConnectedPhoneNearBy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .ambientMode(state.isAmbientMode, state.burnInProtectionRequired),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                RunDataCard(
                    title = stringResource(R.string.heart_rate),
                    value = if (state.canTrackHeartRate) state.heartRate.toFormattedHeartRate() else stringResource(
                        R.string.unsupported
                    ),
                    valueTextColor = if (state.canTrackHeartRate) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                RunDataCard(
                    title = stringResource(R.string.distance),
                    value = (state.distanceMeters / 1000.0).toFormattedKm(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.elapsedDuration.formatted(),
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isTracking) {
                    ToggleRunButton(isRunAction = state.isRunActive, onClick = {
                        onAction(TrackerAction.OnToggleRunClick)
                    })
                    if (!state.isRunActive && state.hasStartedRunning) {
                        FilledTonalIconButton(
                            onClick = {
                                onAction(TrackerAction.OnFinishRunClick)
                            }, colors = IconButtonDefaults.filledTonalIconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(imageVector = FinishIcon, contentDescription = null)
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.open_active_run_screen),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = ExclamationMarkIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(text = stringResource(R.string.connect_your_phone), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ToggleRunButton(isRunAction: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedIconButton(onClick = onClick, modifier = modifier) {
        val icon = if (isRunAction) PauseIcon else StartIcon
        val contentDescription =
            if (isRunAction) stringResource(R.string.pause_run) else stringResource(R.string.start_run)
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}