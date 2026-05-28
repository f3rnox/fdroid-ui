package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val iconUrl: String,
    val versionName: String,
    val totalSize: Long,
    val progress: Float = 0f,
    val status: String = "PENDING", // PENDING, DOWNLOADING, COMPLETED, FAILED
    val apkLocalPath: String? = null
)
