package com.muhammad.run.presentation.active_run

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhammad.core.notification.ActiveRunService
import com.muhammad.core.presentation.designsystem.StartIcon
import com.muhammad.core.presentation.designsystem.StopIcon
import com.muhammad.core.presentation.designsystem.components.RunwellActionButton
import com.muhammad.core.presentation.designsystem.components.RunwellDialog
import com.muhammad.core.presentation.designsystem.components.RunwellFloatingActionButton
import com.muhammad.core.presentation.designsystem.components.RunwellOutlinedActionButton
import com.muhammad.core.presentation.designsystem.components.RunwellScafford
import com.muhammad.core.presentation.designsystem.components.RunwellToolbar
import com.muhammad.core.presentation.ui.ObserveAsEvents
import com.muhammad.run.presentation.R
import com.muhammad.run.presentation.active_run.components.RunDataCard
import com.muhammad.run.presentation.active_run.maps.TrackerMap
import com.muhammad.run.presentation.util.hasLocationPermission
import com.muhammad.run.presentation.util.hasNotificationPermission
import com.muhammad.run.presentation.util.shouldShowLocationPermissionRationale
import com.muhammad.run.presentation.util.showShowNotificationPermissionRationale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ActiveRunScreen(
    onFinish: () -> Unit,
    onBack: () -> Unit,
    onServiceToggle: (Boolean) -> Unit,
    viewModel: ActiveRunViewModel = koinViewModel(),
) {
    val context= LocalContext.current
    ObserveAsEvents(viewModel.events) {event ->
        when(event){
            is ActiveRunEvent.Error ->{
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_SHORT).show()
            }
            ActiveRunEvent.RunSaved -> onFinish()
        }
    }
    ActiveRunScreenContent(state = viewModel.state, onServiceToggle = onServiceToggle, onAction = {action ->
        when(action){
            is ActiveRunAction.OnBackClick ->{
                if(!viewModel.state.hasStartingRunning){
                    onBack()
                }
            }
            else -> Unit
        }
        viewModel.onAction(action)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveRunScreenContent(
    state: ActiveRunState,
    onServiceToggle: (Boolean) -> Unit, onAction: (ActiveRunAction) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val hasCourseLocationPermission =
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val hasFineLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val hasNotificationPermission = if (Build.VERSION.SDK_INT >= 33) {
                perms[Manifest.permission.POST_NOTIFICATIONS] == true
            } else true
            val showLocationRationale = activity.showShowNotificationPermissionRationale()
            val showNotificationRationale = activity.showShowNotificationPermissionRationale()
            onAction(
                ActiveRunAction.SubmitLocationPermissionInfo(
                    acceptedLocationPermission = hasFineLocationPermission && hasCourseLocationPermission,
                    showLocationRationale = showLocationRationale
                )
            )
            onAction(
                ActiveRunAction.SubmitNotificationPermissionInfo(
                    acceptedNotificationPermission = hasNotificationPermission,
                    showNotificationRationale = showNotificationRationale
                )
            )
        }
    LaunchedEffect(true) {
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.showShowNotificationPermissionRationale()
        onAction(
            ActiveRunAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = context.hasLocationPermission(),
                showLocationRationale = showLocationRationale
            )
        )
        onAction(
            ActiveRunAction.SubmitNotificationPermissionInfo(
                acceptedNotificationPermission = context.hasNotificationPermission(),
                showNotificationRationale = showNotificationRationale
            )
        )
        if (!showLocationRationale && !showNotificationRationale) {
            permissionLauncher.requestRunwellPermissions(context)
        }
    }
    val isServiceActive by ActiveRunService.isServiceActive.collectAsStateWithLifecycle()
    LaunchedEffect(state.shouldTrack, isServiceActive) {
        if (context.hasNotificationPermission() && state.shouldTrack && !isServiceActive) {
            onServiceToggle(true)
        }
    }
    RunwellScafford(withGradient = false, topAppBar = {
        RunwellToolbar(
            showBackButton = true,
            title = stringResource(R.string.active_run),
            onBackClick = {
                onAction(ActiveRunAction.OnBackClick)
            })
    }, floatingActionButton = {
        RunwellFloatingActionButton(
            icon = if (state.shouldTrack) StopIcon else StartIcon, onClick = {
                onAction(ActiveRunAction.OnToggleRunClick)
            }, iconSize = 20.dp, contentDescription = null
        )
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TrackerMap(
                isRunFinished = state.isRunFinished,
                currentLocation = state.currentLocation,
                onSnapshot = { bit ->
                    val stream = ByteArrayOutputStream()
                    stream.use {
                        bit.compress(Bitmap.CompressFormat.JPEG, 80, it)
                    }
                    onAction(ActiveRunAction.OnRunProcessed(stream.toByteArray()))
                }, modifier = Modifier.fillMaxSize(), locations = state.runData.locations
            )
            RunDataCard(
                elapsedTime = state.elapsedTime,
                runData = state.runData,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(paddingValues)
                    .fillMaxWidth()
            )
        }
    }
    if (!state.shouldTrack && state.hasStartingRunning) {
        RunwellDialog(title = stringResource(R.string.running_is_paused), onDismiss = {
            onAction(ActiveRunAction.OnResumeRunClick)
        }, description = stringResource(R.string.resume_or_finish_run), primaryButton = {
            RunwellActionButton(
                text = stringResource(R.string.resume),
                isLoading = false,
                onClick = {
                    onAction(ActiveRunAction.OnResumeRunClick)
                },
                modifier = Modifier.weight(1f)
            )
        }, secondaryButton = {
            RunwellOutlinedActionButton(
                text = stringResource(R.string.finish),
                isLoading = state.isSavingRun,
                modifier = Modifier.weight(1f)
            ) {
                onAction(ActiveRunAction.OnFinishRunClick)
            }
        })
    }
    if (state.showLocationRationale || state.showNotificationRationale) {
        RunwellDialog(
            title = stringResource(R.string.permission_required),
            onDismiss = {},
            description = when {
                state.showLocationRationale && state.showNotificationRationale -> {
                    stringResource(R.string.location_notification_rationale)
                }

                state.showLocationRationale -> {
                    stringResource(R.string.location_rationale)
                }

                else -> stringResource(R.string.notification_rationale)
            },
            primaryButton = {
                RunwellOutlinedActionButton(
                    text = stringResource(R.string.okay),
                    isLoading = false,
                    onClick = {
                        onAction(ActiveRunAction.DismissRationaleDialog)
                        permissionLauncher.requestRunwellPermissions(context)
                    })
            })
    }
}

private fun ActivityResultLauncher<Array<String>>.requestRunwellPermissions(context: Context) {
    val hasLocationPermission = context.hasLocationPermission()
    val hasNotificationPermission = context.hasLocationPermission()
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val notificationPermissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else arrayOf()
    when {
        !hasLocationPermission && !hasNotificationPermission -> {
            launch(locationPermissions + notificationPermissions)
        }

        !hasLocationPermission -> {
            launch(locationPermissions)
        }

        !hasNotificationPermission -> {
            launch(notificationPermissions)
        }
    }
}