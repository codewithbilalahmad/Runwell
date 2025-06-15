package com.muhammad.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.muhammad.core.database.dao.AnalyticsDao
import com.muhammad.core.database.dao.RunDao
import com.muhammad.core.database.dao.RunPendingSyncDao
import com.muhammad.core.database.entity.DeletedRunSyncEntity
import com.muhammad.core.database.entity.RunEntity
import com.muhammad.core.database.entity.RunPendingSyncEntity

@Database(
    entities = [
        RunEntity::class,
        RunPendingSyncEntity::class,
        DeletedRunSyncEntity::class
    ], version = 1
)
abstract class RunDatabase : RoomDatabase(){
    abstract val runDao : RunDao
    abstract val runPendingSyncDao : RunPendingSyncDao
    abstract val analyticsDao : AnalyticsDao
}