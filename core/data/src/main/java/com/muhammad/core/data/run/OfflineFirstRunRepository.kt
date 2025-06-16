package com.muhammad.core.data.run

import com.muhammad.core.data.networking.get
import com.muhammad.core.database.dao.RunPendingSyncDao
import com.muhammad.core.database.mappers.toRun
import com.muhammad.core.domain.SessionStorage
import com.muhammad.core.domain.run.LocalRunDataSource
import com.muhammad.core.domain.run.RemoteRunDataSource
import com.muhammad.core.domain.run.Run
import com.muhammad.core.domain.run.RunRepository
import com.muhammad.core.domain.run.SyncRunScheduler
import com.muhammad.core.domain.util.DataError
import com.muhammad.core.domain.util.EmptyResult
import com.muhammad.core.domain.util.Result
import com.muhammad.core.domain.util.asEmptyDataResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineFirstRunRepository(
    private val localRunLocalRunDataSource: LocalRunDataSource,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val applicationScope : CoroutineScope,
    private val sessionStorage : SessionStorage,
    private val syncRunScheduler: SyncRunScheduler,
    private val runPendingSyncDao : RunPendingSyncDao,
    private val client : HttpClient
) : RunRepository{
    override fun getRuns(): Flow<List<Run>> {
       return localRunLocalRunDataSource.getRuns()
    }

    override suspend fun fetchRuns(): EmptyResult<DataError> {
        return when(val result = remoteRunDataSource.getRuns()){
            is Result.Failure -> result.asEmptyDataResult()
            is Result.Success ->{
                applicationScope.async {
                    localRunLocalRunDataSource.upsertRuns(result.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun upsertRun(
        run: Run,
        mapPicture: ByteArray,
    ): EmptyResult<DataError> {
        val localResult = localRunLocalRunDataSource.upsertRun(run)
        if(localResult !is Result.Success){
            return localResult.asEmptyDataResult()
        }
        val runWithId = run.copy(id = localResult.data)
        val remoteResult = remoteRunDataSource.postRun(
            run = runWithId, mapPicture = mapPicture
        )
        return when(remoteResult){
            is Result.Failure -> {
                applicationScope.launch {
                    syncRunScheduler.scheduleSync(
                        type = SyncRunScheduler.SyncType.CreateRun(
                            run = runWithId, mapPictureBytes = mapPicture
                        )
                    )
                }.join()
                Result.Success(Unit)
            }
            is Result.Success -> {
                applicationScope.async {
                    localRunLocalRunDataSource.upsertRun(remoteResult.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun deleteRun(id: String) {
        localRunLocalRunDataSource.deleteRun(id)
        val isPendingSync = runPendingSyncDao.getRunPendingSyncEntity(id) != null
        if(isPendingSync){
            runPendingSyncDao.deleteRunPendingSyncEntity(id)
            return
        }
        val remoteResult = applicationScope.async {
            remoteRunDataSource.deleteRun(id)
        }.await()
        if(remoteResult is Result.Failure){
            applicationScope.async {
                syncRunScheduler.scheduleSync(
                    type = SyncRunScheduler.SyncType.DeleteRun(id)
                )
            }.join()
        }
    }

    override suspend fun syncPendingRuns() {
        withContext(Dispatchers.IO){
            val userId = sessionStorage.get()?.userId ?: return@withContext
            val createdRuns = async {
                runPendingSyncDao.getAllRunPendingSyncEntities(userId = userId)
            }
            val deletedRuns =async {
                runPendingSyncDao.getAllDeletedRunSyncEntities(userId)
            }
            val createJobs=  createdRuns.await().map {
                launch {
                    val run = it.run.toRun()
                    when(remoteRunDataSource.postRun(run, it.mapPictureBytes)){
                        is Result.Failure -> Unit
                        is Result.Success -> {
                            applicationScope.launch {
                                runPendingSyncDao.deleteRunPendingSyncEntity(it.runId)
                            }.join()
                        }
                    }
                }
            }
            val deleteJobs= deletedRuns.await().map {
                launch {
                    when(remoteRunDataSource.deleteRun(it.runId)){
                        is Result.Failure -> Unit
                        is Result.Success -> {
                            applicationScope.launch {
                                runPendingSyncDao.deleteDeletedRunSyncEntity(it.runId)
                            }.join()
                        }
                    }
                }
            }
            createJobs.forEach { it.join() }
            deleteJobs.forEach { it.join() }
        }
    }

    override suspend fun deleteAllRuns() {
        localRunLocalRunDataSource.deleteAllRuns()
    }

    override suspend fun logout(): EmptyResult<DataError.Network> {
        val result = client.get<Unit>(route = "/logout").asEmptyDataResult()
        client.authProviders.filterIsInstance<BearerAuthProvider>().firstOrNull()?.clearToken()
        return result
    }
}