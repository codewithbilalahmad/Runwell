package com.muhammad.core.domain.run

import kotlin.time.Duration

interface SyncRunScheduler{
    suspend fun scheduleSync(type : SyncType)
    suspend fun cancelAllSync()
    sealed interface SyncType{
        data class FetchRuns(val interval : Duration) : SyncType
        data class DeleteRun(val runId : RunId) : SyncType
        class CreateRun(val run : Run, val mapPictureBytes : ByteArray) : SyncType
    }
}