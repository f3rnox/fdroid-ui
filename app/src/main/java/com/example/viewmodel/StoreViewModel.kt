package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AppEntity
import com.example.data.model.DownloadEntity
import com.example.data.model.RepositoryEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val appDatabase = AppDatabase.getDatabase(application)
    private val repository = AppRepository(application, appDatabase.appDao())

    // UI state streams from Room persistence
    val repos: StateFlow<List<RepositoryEntity>> = repository.allRepos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApps: StateFlow<List<AppEntity>> = repository.allApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteApps: StateFlow<List<AppEntity>> = repository.favoriteApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Sync progress tracking state
    private val _syncingRepoUrl = MutableStateFlow<String?>(null)
    val syncingRepoUrl = _syncingRepoUrl.asStateFlow()

    private val _syncProgress = MutableStateFlow("")
    val syncProgress = _syncProgress.asStateFlow()

    init {
        // Automatically seed/populate DB on first run in the background
        viewModelScope.launch(Dispatchers.Default) {
            repository.seedDatabaseIfEmpty()

            // Automatically sync repositories on first startup sequentially to prevent race conditions
            val sharedPrefs = getApplication<Application>().getSharedPreferences("store_prefs", android.content.Context.MODE_PRIVATE)
            val firstSyncDone = sharedPrefs.getBoolean("first_startup_sync_completed", false)
            if (!firstSyncDone) {
                val reposList = repository.allRepos.first()
                if (reposList.isNotEmpty()) {
                    reposList.forEach { repo ->
                        _syncingRepoUrl.value = repo.url
                        _syncProgress.value = "Auto-syncing ${repo.name}..."
                        repository.syncRepository(repo.url) { progressMsg ->
                            _syncProgress.value = "${repo.name}: $progressMsg"
                        }
                    }
                    _syncingRepoUrl.value = null
                    _syncProgress.value = ""
                    sharedPrefs.edit().putBoolean("first_startup_sync_completed", true).apply()
                }
            }
        }
    }

    // Filtered apps combining search + category
    val filteredApps: StateFlow<List<AppEntity>> = combine(
        allApps,
        _searchQuery,
        _selectedCategory
    ) { apps, query, category ->
        apps.filter { app ->
            val matchesSearch = query.isEmpty() || 
                    app.name.contains(query, ignoreCase = true) || 
                    app.packageName.contains(query, ignoreCase = true) ||
                    app.summary.contains(query, ignoreCase = true)
            
            val matchesCategory = category == "All" || app.category.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            repository.toggleFavorite(packageName)
        }
    }

    fun addRepo(name: String, url: String) {
        viewModelScope.launch {
            repository.addRepo(name, url)
        }
    }

    fun deleteRepo(url: String) {
        viewModelScope.launch {
            repository.deleteRepo(url)
        }
    }

    fun syncRepo(url: String) {
        viewModelScope.launch {
            _syncingRepoUrl.value = url
            repository.syncRepository(url) { message ->
                _syncProgress.value = message
            }
            _syncingRepoUrl.value = null
            _syncProgress.value = ""
        }
    }

    private val _downloadStatus = MutableStateFlow<Map<String, String>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    fun downloadApp(packageName: String) {
        viewModelScope.launch {
            repository.downloadAppApk(packageName) { progress, statusMsg ->
                _downloadProgress.update { it + (packageName to progress) }
                _downloadStatus.update { it + (packageName to statusMsg) }
            }
        }
    }

    fun installApp(packageName: String) {
        repository.installAppApk(packageName)
    }

    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            repository.uninstallApp(packageName)
            _downloadProgress.update { it - packageName }
            _downloadStatus.update { it - packageName }
        }
    }

    fun getAppFlow(packageName: String): Flow<AppEntity?> = repository.getAppFlow(packageName)

    fun seedRepoDirectReset() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }
}
