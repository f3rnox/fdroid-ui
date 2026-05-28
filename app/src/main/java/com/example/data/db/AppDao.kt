package com.example.data.db

import androidx.room.*
import com.example.data.model.AppEntity
import com.example.data.model.DownloadEntity
import com.example.data.model.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Repository management
    @Query("SELECT * FROM repositories")
    fun getAllRepos(): Flow<List<RepositoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepo(repo: RepositoryEntity)

    @Query("DELETE FROM repositories WHERE url = :url")
    suspend fun deleteRepo(url: String)

    @Query("UPDATE repositories SET lastSyncTime = :syncTime, appCount = :count WHERE url = :url")
    suspend fun updateRepoSync(url: String, syncTime: Long, count: Int)

    // App management
    @Query("SELECT * FROM apps ORDER BY name ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE category = :category ORDER BY name ASC")
    fun getAppsByCategory(category: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): AppEntity?

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    fun getAppFlowByPackage(packageName: String): Flow<AppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("UPDATE apps SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE apps SET isInstalled = :isInstalled WHERE packageName = :packageName")
    suspend fun updateInstalledState(packageName: String, isInstalled: Boolean)

    @Query("DELETE FROM apps WHERE repoUrl = :repoUrl")
    suspend fun clearAppsByRepo(repoUrl: String)

    // Downloads management
    @Query("SELECT * FROM downloads ORDER BY appName ASC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, status = :status WHERE packageName = :packageName")
    suspend fun updateDownloadStatus(packageName: String, progress: Float, status: String)

    @Query("DELETE FROM downloads WHERE packageName = :packageName")
    suspend fun deleteDownload(packageName: String)
}
