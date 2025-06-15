package com.muhammad.core.database.dao

import androidx.room.*
import com.muhammad.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao{
    @Upsert
    suspend fun upsertRun(run : RunEntity)
    @Upsert
    suspend fun upsertRuns(runs : List<RunEntity>)
    @Query("SELECT * FROM runentity ORDER BY dateTimeUTC DESC")
    fun getRuns() : Flow<List<RunEntity>>
    @Query("DELETE FROM runentity WHERE id =:id")
    suspend fun deleteRun(id : String)
    @Query("DELETE FROM runentity")
    suspend fun deleteAllRuns()
}