package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepositoryEntity
import com.example.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoManagerScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val repos by viewModel.repos.collectAsState()
    val syncingRepoUrl by viewModel.syncingRepoUrl.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newRepoName by remember { mutableStateOf("") }
    var newRepoUrl by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REPOSITORIES",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage Sources",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_repo_open_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Repo")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add")
                }
            }
        }

        // Active syncing banner
        AnimatedVisibility(
            visible = syncingRepoUrl != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Synchronizing Repository",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = syncProgress,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        if (repos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CloudOff,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No feeds or repositories added.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(repos, key = { it.url }) { repo ->
                    RepoRowItem(
                        repo = repo,
                        isSyncing = syncingRepoUrl == repo.url,
                        onSyncClick = { viewModel.syncRepo(repo.url) },
                        onDeleteClick = { viewModel.deleteRepo(repo.url) }
                    )
                }
            }
        }
    }

    // Modal adding dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Repository", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newRepoName,
                        onValueChange = { newRepoName = it },
                        label = { Text("Repository Name") },
                        placeholder = { Text("e.g. My Custom Feed") },
                        modifier = Modifier.fillMaxWidth().testTag("add_repo_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newRepoUrl,
                        onValueChange = { newRepoUrl = it },
                        label = { Text("Repository Address (URL)") },
                        placeholder = { Text("e.g. https://root.com/fdroid/repo") },
                        modifier = Modifier.fillMaxWidth().testTag("add_repo_url_input"),
                        singleLine = true
                    )
                    Text(
                        text = "F-Droid repositories typically end with an address folder like '/fdroid/repo' or '/repo'.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRepoName.isNotBlank() && newRepoUrl.isNotBlank()) {
                            viewModel.addRepo(newRepoName, newRepoUrl)
                            newRepoName = ""
                            newRepoUrl = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("add_repo_confirm_btn")
                ) {
                    Text("Add Source")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RepoRowItem(
    repo: RepositoryEntity,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val lastSyncStr = if (repo.lastSyncTime > 0) {
        dateFormat.format(Date(repo.lastSyncTime))
    } else {
        "Never Synced"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repo_card_${repo.url}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repo.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = repo.url,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(2.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onSyncClick,
                            modifier = Modifier.testTag("repo_sync_btn_${repo.url}")
                        ) {
                            Icon(
                                Icons.Filled.Sync,
                                contentDescription = "Sync from repo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Protect F-Droid official repo from accidental deletions
                    if (repo.url != "https://f-droid.org/repo") {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.testTag("repo_delete_btn_${repo.url}")
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Repo",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!repo.description.isNullOrEmpty()) {
                Text(
                    text = repo.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = "Sync schedule",
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Synced: $lastSyncStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${repo.appCount} Apps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// helper inline function to allow standard delegate mutableStateOf usage
@Composable
fun <T> rememberStateOf(value: T): MutableState<T> = remember { mutableStateOf(value) }
