package com.muhammad.run.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.muhammad.core.domain.run.RunRepository

class FetchRunWorker(
    context: Context,
    params : WorkerParameters,
    private val runRepository: RunRepository
) : CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        if(runAttemptCount >= 5){
            return Result.failure()
        }
        return when(val result = runRepository.fetchRuns()){
            is com.muhammad.core.domain.util.Result.Success -> Result.success()
            is com.muhammad.core.domain.util.Result.Failure ->{
                result.error.toWorkerResult()
            }
        }
    }
}