package com.muhammad.core.database.entity

import androidx.room.*

@Entity
data class DeletedRunSyncEntity(
    @PrimaryKey(autoGenerate = false)
    val runId : String,
    val userId : String
)