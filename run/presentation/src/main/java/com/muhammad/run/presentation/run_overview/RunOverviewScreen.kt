package com.muhammad.run.presentation.run_overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhammad.core.presentation.designsystem.AnalyticsIcon
import com.muhammad.core.presentation.designsystem.LogoIcon
import com.muhammad.core.presentation.designsystem.LogoutIcon
import com.muhammad.core.presentation.designsystem.RunIcon
import com.muhammad.core.presentation.designsystem.components.RunwellFloatingActionButton
import com.muhammad.core.presentation.designsystem.components.RunwellScafford
import com.muhammad.core.presentation.designsystem.components.RunwellToolbar
import com.muhammad.core.presentation.designsystem.components.util.DropDownItem
import com.muhammad.run.presentation.R
import com.muhammad.run.presentation.run_overview.components.RunListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunOverviewScreen(
    onStartRunClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    viewModel: RunOverviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RunOverviewScreenContent(state = state) {action ->
        when(action){
            RunOverviewAction.OnAnalyticsClick -> onAnalyticsClick()
            RunOverviewAction.OnLogoutClick -> onLogoutClick()
            RunOverviewAction.OnStartClick -> onStartRunClick()
            else -> Unit
        }
        viewModel.onAction(action)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunOverviewScreenContent(state: RunOverviewState, onAction: (RunOverviewAction) -> Unit) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    RunwellScafford(topAppBar = {
        RunwellToolbar(
            showBackButton = false,
            title = stringResource(R.string.runique),
            scrollBehavior = scrollBehavior,
            menuItems = listOf(
                DropDownItem(icon = AnalyticsIcon, title = stringResource(R.string.analytics)),
                DropDownItem(icon = LogoutIcon, title = stringResource(R.string.logout)),
            ), onMenuItemClick = { index ->
                when (index) {
                    0 -> onAction(RunOverviewAction.OnAnalyticsClick)
                    1 -> onAction(RunOverviewAction.OnLogoutClick)
                }
            }, startContent = {
                Icon(
                    imageVector = LogoIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        )
    }, floatingActionButton = {
        RunwellFloatingActionButton(icon = RunIcon, onClick = {
            onAction(RunOverviewAction.OnStartClick)
        })
    }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = state.runs, key = { it.id }) { run ->
                RunListItem(runUi = run, onDeleteClick = {
                    onAction(RunOverviewAction.DeleteRun(run))
                }, modifier = Modifier.animateItem())
            }
        }
    }
}