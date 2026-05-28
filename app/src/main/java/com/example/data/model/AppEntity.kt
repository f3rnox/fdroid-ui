package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val repoUrl: String,
    val name: String,
    val summary: String,
    val description: String,
    val category: String,
    val iconUrl: String,
    val versionName: String,
    val versionCode: Long,
    val apkUrl: String,
    val sizeBytes: Long,
    val website: String?,
    val sourceCode: String?,
    val issueTracker: String?,
    val license: String?,
    val isFavorite: Boolean = false,
    val isInstalled: Boolean = false,
    val addedDate: Long = System.currentTimeMillis()
)
