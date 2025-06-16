package com.muhammad.run.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.muhammad.core.database.dao.RunPendingSyncDao
import com.muhammad.core.database.mappers.toRun
import com.muhammad.core.domain.run.RemoteRunDataSource

class CreateRunWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val pendingSyncDao: RunPendingSyncDao,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        val pendingRunId = params.inputData.getString(RUN_ID) ?: return Result.failure()
        val pendingRunEntity =
            pendingSyncDao.getRunPendingSyncEntity(pendingRunId) ?: return Result.failure()
        val run = pendingRunEntity.run.toRun()
        return when (val result = remoteRunDataSource.postRun(run = run, mapPicture = pendingRunEntity.mapPictureBytes)) {
            is com.muhammad.core.domain.util.Result.Success -> {
                pendingSyncDao.deleteRunPendingSyncEntity(pendingRunId)
                Result.success()
            }
            is com.muhammad.core.domain.util.Result.Failure ->{
                result.error.toWorkerResult()
            }
        }
    }

    companion object {
        const val RUN_ID = "RUN_ID"
    }
}