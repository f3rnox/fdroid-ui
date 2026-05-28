package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repositories")
data class RepositoryEntity(
    @PrimaryKey val url: String,
    val name: String,
    val description: String?,
    val isEnabled: Boolean = true,
    val lastSyncTime: Long = 0,
    val appCount: Int = 0
)
