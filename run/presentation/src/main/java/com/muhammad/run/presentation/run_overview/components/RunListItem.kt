package com.muhammad.run.presentation.run_overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.muhammad.core.presentation.designsystem.CalendarIcon
import com.muhammad.core.presentation.designsystem.RunOutlinedIcon
import com.muhammad.run.presentation.run_overview.model.RunDataUi
import com.muhammad.run.presentation.run_overview.model.RunUi
import com.muhammad.run.presentation.R
import kotlin.math.max

@Composable
fun RunListItem(modifier: Modifier = Modifier, runUi: RunUi, onDeleteClick: () -> Unit) {
    var showDropDown by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(onClick = {}, onLongClick = {
                    showDropDown = true
                })
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MapImage(imageUrl = runUi.mapPictureUrl)
            RunningTimeSection(duration = runUi.duration, modifier = Modifier.fillMaxWidth())
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            RunningDataSection(dateTime = runUi.dateTime)
            DataGrid(run = runUi, modifier = Modifier.fillMaxWidth())
        }
        DropdownMenu(expanded = showDropDown, onDismissRequest = {
            showDropDown = false
        }) {
            DropdownMenuItem(text = {
                Text(text = stringResource(R.string.delete))
            }, onClick = {
                showDropDown = false
                onDeleteClick()
            })
        }
    }
}

@Composable
private fun MapImage(modifier: Modifier = Modifier, imageUrl: String?) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = stringResource(R.string.run_map),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(15.dp)), loading = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }, error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_couldnt_load_image),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun RunningTimeSection(modifier: Modifier = Modifier, duration: String) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(0.1f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RunOutlinedIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.total_running_time),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = duration, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RunningDataSection(dateTime: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = CalendarIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(dateTime, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DataGrid(modifier: Modifier = Modifier, run: RunUi) {
    val runDataList = listOf(
        RunDataUi(name = stringResource(R.string.distance), value = run.distance),
        RunDataUi(name = stringResource(R.string.pace), value = run.pace),
        RunDataUi(name = stringResource(R.string.avg_speed), value = run.avgSpeed),
        RunDataUi(name = stringResource(R.string.total_elevation), value = run.totalElevation),
        RunDataUi(name = stringResource(R.string.avg_heart_rate), value = run.avgHeartRate),
        RunDataUi(name = stringResource(R.string.max_heart_rate), value = run.maxHeartRate),
    )
    var maxWidth by remember {
        mutableIntStateOf(0)
    }
    val maxWidthDp = with(LocalDensity.current) { maxWidth.toDp() }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        runDataList.forEach { run ->
            DataGridCell(
                runData = run, modifier = Modifier
                    .defaultMinSize(minWidth = maxWidthDp)
                    .onSizeChanged { size ->
                        maxWidth = max(maxWidth, size.width)
                    })
        }
    }
}

@Composable
private fun DataGridCell(modifier: Modifier = Modifier, runData: RunDataUi) {
    Column(modifier = modifier) {
        Text(
            text = runData.name,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(text = runData.value, color = MaterialTheme.colorScheme.onSurface)
    }
}