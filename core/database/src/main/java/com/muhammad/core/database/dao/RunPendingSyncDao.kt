package com.muhammad.core.database.dao

import androidx.room.*
import com.muhammad.core.database.entity.DeletedRunSyncEntity
import com.muhammad.core.database.entity.RunPendingSyncEntity

@Dao
interface RunPendingSyncDao{
    @Query("SELECT * FROM runpendingsyncentity WHERE userId =:userId")
    suspend fun getAllRunPendingSyncEntities(userId : String) : List<RunPendingSyncEntity>
    @Query("SELECT * FROM runpendingsyncentity WHERE runId =:runId")
    suspend fun getRunPendingSyncEntity(runId : String) : RunPendingSyncEntity?
    @Upsert
    suspend fun upsertRunPendingSyncEntity(runPendingSyncEntity: RunPendingSyncEntity)
    @Query("DELETE FROM deletedrunsyncentity WHERE userId=:userId")
    suspend fun deleteRunPendingSyncEntity(userId : String)

    @Query("SELECT * FROM deletedrunsyncentity WHERE userId =:userId")
    suspend fun getAllDeletedRunSyncEntities(userId : String) : List<DeletedRunSyncEntity>
    @Upsert
    suspend fun upsertDeletedRunSyncEntity(deletedRunSyncEntity: DeletedRunSyncEntity)
    @Query("DELETE FROM deletedrunsyncentity WHERE runId =:runId")
    suspend fun deleteDeletedRunSyncEntity(runId : String)


}