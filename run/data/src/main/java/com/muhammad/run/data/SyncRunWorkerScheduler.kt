package com.muhammad.run.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.muhammad.core.database.dao.RunPendingSyncDao
import com.muhammad.core.database.entity.DeletedRunSyncEntity
import com.muhammad.core.database.entity.RunPendingSyncEntity
import com.muhammad.core.database.mappers.toRunEntity
import com.muhammad.core.domain.SessionStorage
import com.muhammad.core.domain.run.Run
import com.muhammad.core.domain.run.RunId
import com.muhammad.core.domain.run.SyncRunScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.toJavaDuration

class SyncRunWorkerScheduler(
    private val context: Context,
    private val pendingSyncDao: RunPendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
) : SyncRunScheduler {
    private val workManager = WorkManager.getInstance(context)
    override suspend fun scheduleSync(type: SyncRunScheduler.SyncType) {
        when(type){
            is SyncRunScheduler.SyncType.CreateRun -> scheduleCreateRunWorker(type.run, type.mapPictureBytes)
            is SyncRunScheduler.SyncType.DeleteRun -> scheduleDeleteRunWorker(type.runId)
            is SyncRunScheduler.SyncType.FetchRuns -> scheduleFetchRunWorker(interval = type.interval)
        }
    }

    private suspend fun scheduleDeleteRunWorker(runId: RunId) {
        val userId = sessionStorage.get()?.userId ?: return
        val entity = DeletedRunSyncEntity(
            runId = runId, userId = userId
        )
        pendingSyncDao.upsertDeletedRunSyncEntity(entity)
        val workRequest =
            OneTimeWorkRequestBuilder<DeleteRunWorker>().addTag("delete_work").setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L, timeUnit = TimeUnit.MILLISECONDS
            ).setInputData(
                Data.Builder().putString(DeleteRunWorker.RUN_ID, entity.runId).build()
            ).build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleCreateRunWorker(run: Run, mapPictureByteArray: ByteArray) {
        val userId = sessionStorage.get()?.userId ?: return
        val pendingRun = RunPendingSyncEntity(
            run = run.toRunEntity(), mapPictureBytes = mapPictureByteArray, userId = userId
        )
        pendingSyncDao.upsertRunPendingSyncEntity(pendingRun)
        val workRequest =
            OneTimeWorkRequestBuilder<CreateRunWorker>().addTag("create_work").setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MICROSECONDS
            ).setInputData(
                Data.Builder().putString(CreateRunWorker.RUN_ID, pendingRun.runId).build()
            ).build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleFetchRunWorker(interval: kotlin.time.Duration) {
        val isSyncScheduled = withContext(Dispatchers.IO) {
            workManager.getWorkInfosByTag("sync_work").get().isNotEmpty()
        }
        if (isSyncScheduled) {
            return
        }
        val workRequest =
            PeriodicWorkRequestBuilder<FetchRunWorker>(repeatInterval = interval.toJavaDuration()).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setBackoffCriteria(
                backoffDelay = 2000L,
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                timeUnit = TimeUnit.MICROSECONDS
            ).setInitialDelay(duration = 30, timeUnit = TimeUnit.MINUTES).addTag("sync_work")
                .build()
        workManager.enqueue(workRequest).await()
    }

    override suspend fun cancelAllSync() {
        workManager.cancelAllWork().await()
    }
}