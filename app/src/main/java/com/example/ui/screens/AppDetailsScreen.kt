package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AppEntity
import com.example.viewmodel.StoreViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    viewModel: StoreViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appState = viewModel.getAppFlow(packageName).collectAsState(initial = null)
    val app = appState.value

    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val downloadStatusMap by viewModel.downloadStatus.collectAsState()

    val progress = downloadProgressMap[packageName] ?: 0f
    val status = downloadStatusMap[packageName] ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (app != null) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(app.packageName) },
                            modifier = Modifier.testTag("detail_favorite_btn")
                        ) {
                            Icon(
                                imageVector = if (app.isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Toggle Favorite",
                                tint = if (app.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (app == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Header (Icon, Title, Package, Category)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(app.iconUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "${app.name} icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("detail_app_name")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(app.category, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            if (app.license != null) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(app.license, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Installation & Download card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (app.isInstalled) {
                            val isReallyInstalled = remember(app.packageName) {
                                isAppInstalledOnDevice(context, app.packageName)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isReallyInstalled) Icons.Filled.CheckCircle else Icons.Filled.DownloadDone,
                                        contentDescription = if (isReallyInstalled) "Installed" else "Downloaded",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isReallyInstalled) "Installed on Device" else "APK Downloaded",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = if (isReallyInstalled) "Available in system application list" else "Ready to initialize installation",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isReallyInstalled) {
                                    Button(
                                        onClick = {
                                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                            if (launchIntent != null) {
                                                try {
                                                    context.startActivity(launchIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Could not open app: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "${app.name} launch intent not found.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Open")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.installApp(app.packageName)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("install_apk_btn")
                                    ) {
                                        Icon(Icons.Filled.SystemUpdate, contentDescription = "Install", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Install")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isReallyInstalled) {
                                    OutlinedButton(
                                        onClick = {
                                            Toast.makeText(context, "Running ${app.name} pre-packaged simulation model.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Simulate")
                                    }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.uninstallApp(app.packageName) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("uninstall_btn"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Uninstall", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove APK")
                                }
                            }
                        } else if (status == "DOWNLOADING") {
                            Text(
                                text = "Downloading...",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Simulating APK transmission",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Version v${app.versionName}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${String.format("%.1f", app.sizeBytes.toFloat() / (1024 * 1024))} MB",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Button(
                                    onClick = { viewModel.downloadApp(app.packageName) },
                                    modifier = Modifier.testTag("download_install_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = "Download")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

// Summary text box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = app.summary,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description Title
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // App Long Description
                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Metadata Section (Links & Sources)
                Text(
                    text = "Application Metadata",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                MetaLinkRow(
                    label = "Official Website",
                    value = app.website,
                    icon = Icons.Filled.Language,
                    onClick = {
                        try {
                            val webpage: Uri = Uri.parse(app.website)
                            val intent = Intent(Intent.ACTION_VIEW, webpage)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No web browser found to open link.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                MetaLinkRow(
                    label = "Source Code",
                    value = app.sourceCode,
                    icon = Icons.Filled.Code,
                    onClick = {
                        try {
                            val webpage: Uri = Uri.parse(app.sourceCode)
                            val intent = Intent(Intent.ACTION_VIEW, webpage)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No web browser found to open link.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                MetaLinkRow(
                    label = "Issue Tracker",
                    value = app.issueTracker,
                    icon = Icons.Filled.BugReport,
                    onClick = {
                        try {
                            val webpage: Uri = Uri.parse(app.issueTracker)
                            val intent = Intent(Intent.ACTION_VIEW, webpage)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No web browser found to open link.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun MetaLinkRow(
    label: String,
    value: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    if (!value.isNullOrEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = value,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Open Web URL",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

fun isAppInstalledOnDevice(context: android.content.Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}
