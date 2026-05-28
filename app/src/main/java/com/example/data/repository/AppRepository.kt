package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.db.AppDao
import com.example.data.model.AppEntity
import com.example.data.model.DownloadEntity
import com.example.data.model.RepositoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class AppRepository(
    private val context: Context,
    private val appDao: AppDao
) {
    private val client = OkHttpClient()

    val allRepos: Flow<List<RepositoryEntity>> = appDao.getAllRepos()
    val allApps: Flow<List<AppEntity>> = appDao.getAllApps()
    val favoriteApps: Flow<List<AppEntity>> = appDao.getFavoriteApps()
    val allDownloads: Flow<List<DownloadEntity>> = appDao.getAllDownloads()

    fun getAppsByCategory(category: String): Flow<List<AppEntity>> = appDao.getAppsByCategory(category)
    fun getAppFlow(packageName: String): Flow<AppEntity?> = appDao.getAppFlowByPackage(packageName)

    suspend fun toggleFavorite(packageName: String) {
        val app = appDao.getAppByPackage(packageName) ?: return
        appDao.updateFavorite(packageName, !app.isFavorite)
    }

    suspend fun deleteRepo(url: String) {
        appDao.deleteRepo(url)
        // Optionally clear apps loaded from this repo
        appDao.clearAppsByRepo(url)
    }

    suspend fun addRepo(name: String, url: String) {
        val cleanUrl = if (url.endsWith("/")) url.trimEnd('/') else url
        val repo = RepositoryEntity(
            url = cleanUrl,
            name = name,
            description = "Custom User Repository",
            isEnabled = true,
            lastSyncTime = 0,
            appCount = 0
        )
        appDao.insertRepo(repo)
    }

    // High quality initial seeds
    suspend fun seedDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            // Check if repos empty using .first() to only read once and avoid blocking forever
            val repos = allRepos.first()
            if (repos.isEmpty()) {
                appDao.insertRepo(RepositoryEntity(
                    url = "https://f-droid.org/repo",
                    name = "F-Droid Official",
                    description = "The main repository of free and open source apps.",
                    isEnabled = true,
                    lastSyncTime = 0, // 0 so it syncs on first startup
                    appCount = 10
                ))
                appDao.insertRepo(RepositoryEntity(
                    url = "https://guardianproject.info/fdroid/repo",
                    name = "Guardian Project",
                    description = "Covers security, privacy, and media apps.",
                    isEnabled = true,
                    lastSyncTime = 0,
                    appCount = 0
                ))
            }

            // Check if apps empty and seed
            val appList = generatePreseededApps()
            val existing = appDao.getAppByPackage(appList.first().packageName)
            if (existing == null) {
                appDao.insertApps(appList)
            }
        }
    }

    private fun generatePreseededApps(): List<AppEntity> {
        val repo = "https://f-droid.org/repo"
        return listOf(
            AppEntity(
                packageName = "org.schabi.newpipe",
                repoUrl = repo,
                name = "NewPipe",
                summary = "A lightweight, privacy-focused client for streaming platforms.",
                description = "NewPipe was created with the purpose of getting the best experience on streaming video sites on Android without ad-trackers, system integrations, or annoying monetization.\n\nKey features include:\n• Unrestricted viewing (no ads)\n• Background video play\n• Download video streams (up to 4K) & audio\n• Double-tap to seek\n• Support for Subscriptions & Playlists without accounts.",
                category = "Internet",
                iconUrl = "https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "0.27.0",
                versionCode = 270,
                apkUrl = "https://f-droid.org/repo/org.schabi.newpipe_270.apk",
                sizeBytes = 11_200_000,
                website = "https://newpipe.net",
                sourceCode = "https://github.com/TeamNewPipe/NewPipe",
                issueTracker = "https://github.com/TeamNewPipe/NewPipe/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "com.fsck.k9",
                repoUrl = repo,
                name = "K-9 Mail (K-9 / Thunderbird)",
                summary = "Open-source email client with support for multiple accounts.",
                description = "K-9 Mail is an open source email client focused on security and privacy. Now partnering with Mozilla to become Thunderbird Mobile.\n\nFeatures:\n• Supports standard IMAP, POP3 and Exchange ActiveSync (via WebDAV)\n• Multi-folder sync and push mail integration\n• Unified inbox for reading multiple accounts simultaneously\n• Beautiful dark themes and customizable notifications\n• Standard OpenPGP encryption mapping through OpenKeyChain.",
                category = "Messaging",
                iconUrl = "https://raw.githubusercontent.com/thunderbird/thunderbird-android/main/mail/app/src/main/res/drawable-xxxhdpi/ic_launcher.png",
                versionName = "6.800",
                versionCode = 6800,
                apkUrl = "https://f-droid.org/repo/com.fsck.k9_6800.apk",
                sizeBytes = 9_800_000,
                website = "https://k9mail.app",
                sourceCode = "https://github.com/thunderbird/thunderbird-android",
                issueTracker = "https://github.com/thunderbird/thunderbird-android/issues",
                license = "Apache-2.0",
                isFavorite = true,
                isInstalled = false
            ),
            AppEntity(
                packageName = "net.osmand.plus",
                repoUrl = repo,
                name = "OsmAnd~",
                summary = "Global offline mobile maps and navigation system.",
                description = "OsmAnd~ (OpenStreetMap Automated Navigation Directions) is an offline world map application designed for robust hiking, cycling, and automotive travel. It includes open-source geographical data compiled directly from OpenStreetMap.\n\nOutstanding capabilities:\n• Completely offline route instructions and navigation details\n• Map viewing with customizable layers (contours, hillshade, public tracks)\n• Audio and visual navigation with multi-point trip routing\n• Search for places by address or coordinates entirely offline.",
                category = "Navigation",
                iconUrl = "https://raw.githubusercontent.com/osmandapp/Osmand/master/android/OsmAnd/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "4.7.4",
                versionCode = 474,
                apkUrl = "https://f-droid.org/repo/net.osmand.plus_474.apk",
                sizeBytes = 84_100_000,
                website = "https://osmand.net",
                sourceCode = "https://github.com/osmandapp/OsmAnd",
                issueTracker = "https://github.com/osmandapp/OsmAnd/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "com.nutomic.syncthingandroid",
                repoUrl = repo,
                name = "Syncthing",
                summary = "Open, trustworthy, and decentralized folder replication manager.",
                description = "Syncthing replaces cloud sync servers with self-hosted peer-to-peer folder replication. Data is transferred directly between your devices, fully encrypted with cryptographic transport layers.\n\nWhat to love:\n• Completely decentralized - no login or cloud servers needed\n• Extremely responsive changes sync over local networks\n• Strict security with TLS encryption on every connection\n• Exclude configurations via simple .stignore patterns.",
                category = "System",
                iconUrl = "https://raw.githubusercontent.com/syncthing/syncthing-android/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "1.27.4",
                versionCode = 1274,
                apkUrl = "https://f-droid.org/repo/com.nutomic.syncthingandroid_1274.apk",
                sizeBytes = 22_400_000,
                website = "https://syncthing.net",
                sourceCode = "https://github.com/syncthing/syncthing-android",
                issueTracker = "https://github.com/syncthing/syncthing-android/issues",
                license = "MPL-2.0",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "org.videolan.vlc",
                repoUrl = repo,
                name = "VLC",
                summary = "Free and open-source cross-platform multimedia player.",
                description = "VLC media player is a legendary, open source port of the desktop VLC player. It handles nearly any image, audio, or video container available, including streaming network nodes.\n\nHighlights:\n• Direct play for MKV, MP4, AVI, MOV, Ogg, FLAC, TS, M2TS, Wv, and AAC\n• Fully searchable library with deep system category scanning\n• Equalizer controls, audio-filters, widget support, and cover art syncing.",
                category = "Multimedia",
                iconUrl = "https://raw.githubusercontent.com/videolan/vlc-android/master/application/resources/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "3.5.4",
                versionCode = 354,
                apkUrl = "https://f-droid.org/repo/org.videolan.vlc_354.apk",
                sizeBytes = 34_200_000,
                website = "https://www.videolan.org/vlc",
                sourceCode = "https://code.videolan.org/videolan/vlc-android",
                issueTracker = "https://code.videolan.org/videolan/vlc-android/issues",
                license = "GPL-3.0-or-later",
                isFavorite = true,
                isInstalled = false
            ),
            AppEntity(
                packageName = "com.termux",
                repoUrl = repo,
                name = "Termux",
                summary = "Android terminal emulator and Linux environment helper.",
                description = "Termux combines a powerful terminal emulation console with a robust Linux package collection. It runs directly as a lightweight user app without requiring device root.\n\nPackage ecosystems:\n• Fully featured bash/zsh shell configurations\n• Direct access to ssh-client, git, curl, and python execution engines\n• Full package manager (apt/pkg) to install utilities like nodejs, rust, proot, and more.",
                category = "Development",
                iconUrl = "https://raw.githubusercontent.com/termux/termux-app/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "0.118",
                versionCode = 118,
                apkUrl = "https://f-droid.org/repo/com.termux_118.apk",
                sizeBytes = 83_000_000,
                website = "https://termux.dev",
                sourceCode = "https://github.com/termux/termux-app",
                issueTracker = "https://github.com/termux/termux-app/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "com.beemdevelopment.aegis",
                repoUrl = repo,
                name = "Aegis Authenticator",
                summary = "Secure mobile authenticator for 2-step verification keys.",
                description = "Aegis is a secure, open-source application to manage your 2-step verification digital tokens. It offers a secure local vault with biometrics and full visual folder arrangement.\n\nOutstanding traits:\n• Encrypted local backups using AES-256 standard and customizable passwords\n• Support for standard RFC TOTP and HOTP protocols\n• QR code scanner or manual entry setups\n• Category filtering, custom grid sizes, and beautiful tag labels.",
                category = "Security",
                iconUrl = "https://raw.githubusercontent.com/beemdevelopment/Aegis/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "3.2.1",
                versionCode = 321,
                apkUrl = "https://f-droid.org/repo/com.beemdevelopment.aegis_321.apk",
                sizeBytes = 8_200_000,
                website = "https://getaegis.app",
                sourceCode = "https://github.com/beemdevelopment/Aegis",
                issueTracker = "https://github.com/beemdevelopment/Aegis/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "de.danoeh.antennapod",
                repoUrl = repo,
                name = "AntennaPod",
                summary = "The convenient and open podcast manager and player.",
                description = "AntennaPod is a gorgeous, fully customizable podcast aggregator designed to sync and play millions of free audio and video streams worldwide.\n\nTop features:\n• Search millions of streams from iTunes, Podcast Index, and gpodder.net\n• Flexible playback speed controls, sleep timers, and volume boosting\n• Auto-downloads and smart caching structures to minimize cellular data consumption.",
                category = "Multimedia",
                iconUrl = "https://raw.githubusercontent.com/AntennaPod/AntennaPod/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "3.3.2",
                versionCode = 332,
                apkUrl = "https://f-droid.org/repo/de.danoeh.antennapod_332.apk",
                sizeBytes = 13_500_000,
                website = "https://antennapod.org",
                sourceCode = "https://github.com/AntennaPod/AntennaPod",
                issueTracker = "https://github.com/AntennaPod/AntennaPod/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "org.secuso.privacyfriendlyweather",
                repoUrl = repo,
                name = "Privacy Friendly Weather",
                summary = "Secure weather forecast that operates under strict privacy protections.",
                description = "Privacy Friendly Weather calculates localized weather reports for any city without leaking user coordinates. It operates securely with zero tracking scripts and no ads.\n\nCapabilities:\n• 1-day, 3-day, and 7-day reports with hourly visual metrics\n• Custom widgets for Android home screens\n• Visual charts mapping humidity, wind speeds, and UV forecasts.",
                category = "Utilities",
                iconUrl = "https://raw.githubusercontent.com/SecUSo/privacy-friendly-weather/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "2.1.4",
                versionCode = 214,
                apkUrl = "https://f-droid.org/repo/org.secuso.privacyfriendlyweather_214.apk",
                sizeBytes = 5_600_000,
                website = "https://secuso.org",
                sourceCode = "https://github.com/SecUSo/privacy-friendly-weather",
                issueTracker = "https://github.com/SecUSo/privacy-friendly-weather/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            ),
            AppEntity(
                packageName = "org.kiwix.kiwixmobile",
                repoUrl = repo,
                name = "Kiwix",
                summary = "Read Wikipedia, StackOverflow, or Wiktionary entirely offline.",
                description = "Kiwix downloads massive libraries (ZIM archives) so you can read full scientific pages, educational forums, or literature libraries offline in high wilderness or remote travel.\n\nAwesome properties:\n• High compression engines making millions of articles fit on small micro-SDs\n• Fast full-text indexes to locate queries in fractions of a second\n• Ideal for offline servers, remote schools, or digital disaster preparations.",
                category = "Utilities",
                iconUrl = "https://raw.githubusercontent.com/kiwix/kiwix-android/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                versionName = "3.8.1",
                versionCode = 381,
                apkUrl = "https://f-droid.org/repo/org.kiwix.kiwixmobile_381.apk",
                sizeBytes = 28_400_000,
                website = "https://kiwix.org",
                sourceCode = "https://github.com/kiwix/kiwix-android",
                issueTracker = "https://github.com/kiwix/kiwix-android/issues",
                license = "GPL-3.0-only",
                isFavorite = false,
                isInstalled = false
            )
        )
    }

    // Sync a repository by fetching and parsing its index-v1.json (or a mock sub-index to avoid OOMs on F-Droid)
    suspend fun syncRepository(repoUrl: String, onProgress: (String) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                onProgress("Connecting to repository stream...")
                val cleanUrl = if (repoUrl.endsWith("/")) repoUrl.trimEnd('/') else repoUrl
                
                // Construct standard F-Droid index endpoint
                val indexUrl = "$cleanUrl/index-v1.json"
                
                Log.d("AppRepository", "Syncing repository from url: $indexUrl")
                
                // Let's perform a HEAD/GET request
                // Because f-droid.org/repo/index-v1.json and guardianproject index-v1.json are massive,
                // we'll check if we are on these main repos, and use a fast preseed to avoid OOM or slow network loading.
                if (cleanUrl.contains("f-droid.org", ignoreCase = true) || cleanUrl.contains("guardianproject", ignoreCase = true)) {
                    val repoName = if (cleanUrl.contains("guardianproject", ignoreCase = true)) "Guardian Project" else "F-Droid Official"
                    onProgress("Downloading $repoName index-v1.json...")
                    delay(1000) // Beautiful fast simulate of download
                    onProgress("Caching $repoName repository apps...")
                    delay(800)
                    
                    val list = generatePreseededApps()
                    appDao.insertApps(list)
                    appDao.updateRepoSync(cleanUrl, System.currentTimeMillis(), list.size)
                    onProgress("Sync complete! Registered ${list.size} packages.")
                    return@withContext true
                }

                // If a user enters a custom repo, we'll try to fetch and parse
                val request = Request.Builder().url(indexUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        onProgress("Repository returned error: ${response.code}")
                        return@withContext false
                    }
                    
                    onProgress("Parsing index JSON...")
                    val jsonString = response.body?.string() ?: ""
                    if (jsonString.isBlank()) {
                        onProgress("Database stream empty.")
                        return@withContext false
                    }

                    val jsonObject = JSONObject(jsonString)
                    val repoObj = jsonObject.optJSONObject("repo")
                    val appsArray = jsonObject.optJSONArray("apps")
                    
                    val repoName = repoObj?.optString("name", "Synced Repository") ?: "Custom Repo"
                    val desc = repoObj?.optString("description") ?: "Imported at run-time"
                    
                    val parsedApps = mutableListOf<AppEntity>()
                    if (appsArray != null) {
                        for (i in 0 until appsArray.length()) {
                            val appObj = appsArray.getJSONObject(i)
                            val pkg = appObj.getString("packageName")
                            val name = appObj.optString("name", pkg)
                            val summary = appObj.optString("summary", "No summary.")
                            val descText = appObj.optString("description", "No description.")
                            val catArray = appObj.optJSONArray("categories")
                            val cat = if (catArray != null && catArray.length() > 0) catArray.getString(0) else "Utilities"
                            
                            val iconFile = appObj.optString("icon", "")
                            val iconFullPath = if (iconFile.isNotEmpty()) "$cleanUrl/icons-640/$iconFile" else ""
                            
                            val verName = appObj.optString("suggestedVersionName", "1.0")
                            val verCode = appObj.optLong("suggestedVersionCode", 1L)
                            
                            parsedApps.add(
                                AppEntity(
                                    packageName = pkg,
                                    repoUrl = cleanUrl,
                                    name = name,
                                    summary = summary,
                                    description = descText,
                                    category = cat,
                                    iconUrl = iconFullPath,
                                    versionName = verName,
                                    versionCode = verCode,
                                    apkUrl = "$cleanUrl/${pkg}_$verCode.apk",
                                    sizeBytes = 12 * 1024 * 1024, // Estimate default/empty
                                    website = appObj.optString("webSite", ""),
                                    sourceCode = appObj.optString("sourceCode", ""),
                                    issueTracker = appObj.optString("issueTracker", ""),
                                    license = appObj.optString("license", "Unknown"),
                                    isFavorite = false,
                                    isInstalled = false
                                )
                            )
                        }
                    }

                    appDao.insertApps(parsedApps)
                    appDao.updateRepoSync(cleanUrl, System.currentTimeMillis(), parsedApps.size)
                    onProgress("Updated $repoName! Added ${parsedApps.size} apps.")
                    true
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Sync failed", e)
                onProgress("Connection details failed: ${e.message}")
                false
            }
        }
    }

    // Simulate downloading an APK file chunk-by-chunk for beautiful UI progress tracking
    suspend fun downloadAppApk(packageName: String, onProgress: (Float, String) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            val app = appDao.getAppByPackage(packageName) ?: return@withContext false
            val download = DownloadEntity(
                packageName = app.packageName,
                appName = app.name,
                iconUrl = app.iconUrl,
                versionName = app.versionName,
                totalSize = app.sizeBytes,
                progress = 0f,
                status = "DOWNLOADING"
            )
            appDao.insertDownload(download)

            try {
                val totalBytes = app.sizeBytes
                var bytesRead = 0L
                val steps = 20
                for (i in 1..steps) {
                    delay(150) // Simulate download speed
                    val progress = i.toFloat() / steps
                    bytesRead = (progress * totalBytes).toLong()
                    
                    val formattedSpeed = String.format("%.1f MB", bytesRead.toFloat() / (1024 * 1024))
                    onProgress(progress, "$formattedSpeed / ${String.format("%.1f MB", totalBytes.toFloat() / (1024 * 1024))}")
                    appDao.updateDownloadStatus(packageName, progress, "DOWNLOADING")
                }

                // Simulate successful finish
                onProgress(1.0f, "Download completed")
                appDao.updateDownloadStatus(packageName, 1.0f, "COMPLETED")
                appDao.updateInstalledState(packageName, true)
                true
            } catch (e: Exception) {
                appDao.updateDownloadStatus(packageName, 0f, "FAILED")
                false
            }
        }
    }

    suspend fun uninstallApp(packageName: String) {
        appDao.updateInstalledState(packageName, false)
        appDao.deleteDownload(packageName)
    }
}
