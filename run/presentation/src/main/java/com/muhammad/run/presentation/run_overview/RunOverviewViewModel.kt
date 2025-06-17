package com.muhammad.run.presentation.run_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.core.domain.SessionStorage
import com.muhammad.core.domain.run.RunRepository
import com.muhammad.core.domain.run.SyncRunScheduler
import com.muhammad.run.presentation.run_overview.mapper.toRunUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class RunOverviewViewModel(
    private val runRepository: RunRepository,
    private val syncRunScheduler: SyncRunScheduler,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
) : ViewModel() {
    private val _state = MutableStateFlow(RunOverviewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            syncRunScheduler.scheduleSync(
                type = SyncRunScheduler.SyncType.FetchRuns(30.seconds),
            )
        }
        runRepository.getRuns().onEach { runs ->
            val runUI = runs.map { it.toRunUi() }
            _state.update { it.copy(runs = runUI) }
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            runRepository.syncPendingRuns()
            runRepository.fetchRuns()
        }
    }

    fun onAction(action: RunOverviewAction) {
        when (action) {
            RunOverviewAction.OnStartClick -> Unit
            RunOverviewAction.OnLogoutClick -> logout()
            is RunOverviewAction.DeleteRun -> {
                viewModelScope.launch {
                    runRepository.deleteRun(action.runUi.id)
                }
            }
            else -> Unit
        }
    }

    private fun logout() {
        applicationScope.launch {
            syncRunScheduler.cancelAllSync()
            runRepository.deleteAllRuns()
            runRepository.logout()
            sessionStorage.set(null)
        }
    }
}