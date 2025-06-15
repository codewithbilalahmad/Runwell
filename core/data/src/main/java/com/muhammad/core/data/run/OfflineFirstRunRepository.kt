package com.muhammad.core.data.run

import com.muhammad.core.domain.run.LocalRunDataSource
import com.muhammad.core.domain.run.RemoteRunDataSource
import kotlinx.coroutines.CoroutineScope

class OfflineFirstRunRepository(
    private val localRunLocalRunDataSource: LocalRunDataSource,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val applicationScope : CoroutineScope,
    private val runPendingSyncDao : RunPendingSyncDao
){
}