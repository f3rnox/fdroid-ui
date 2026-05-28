package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AppDetailsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.RepoManagerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val storeViewModel: StoreViewModel = viewModel()
                val currentTabState = remember { mutableStateOf(0) } // 0 = Discover, 1 = Library/Downloads, 2 = Repositories
                val selectedAppPackageState = remember { mutableStateOf<String?>(null) }

                val currentTab = currentTabState.value
                val selectedAppPackage = selectedAppPackageState.value

                // Intercept back button when inspecting details
                if (selectedAppPackage != null) {
                    BackHandler {
                        selectedAppPackageState.value = null
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (selectedAppPackage != null) {
                        AppDetailsScreen(
                            packageName = selectedAppPackage,
                            viewModel = storeViewModel,
                            onBackClick = { selectedAppPackageState.value = null }
                        )
                    } else {
                        val configuration = LocalConfiguration.current
                        val isTablet = configuration.screenWidthDp >= 600

                        if (isTablet) {
                            // Side-Navigation layout for wider screens / tablets
                            Row(modifier = Modifier.fillMaxSize()) {
                                NavigationRail(
                                    modifier = Modifier.testTag("side_navigation_rail")
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    NavigationRailItem(
                                        selected = currentTab == 0,
                                        onClick = { currentTabState.value = 0 },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 0) Icons.Filled.Explore else Icons.Outlined.Explore,
                                                contentDescription = "Discover"
                                            )
                                        },
                                        label = { Text("Discover") },
                                        modifier = Modifier.testTag("rail_tab_discover")
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    NavigationRailItem(
                                        selected = currentTab == 1,
                                        onClick = { currentTabState.value = 1 },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 1) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                                                contentDescription = "Library"
                                            )
                                        },
                                        label = { Text("Library") },
                                        modifier = Modifier.testTag("rail_tab_library")
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    NavigationRailItem(
                                        selected = currentTab == 2,
                                        onClick = { currentTabState.value = 2 },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 2) Icons.Filled.Dns else Icons.Outlined.Dns,
                                                contentDescription = "Sources"
                                            )
                                        },
                                        label = { Text("Sources") },
                                        modifier = Modifier.testTag("rail_tab_sources")
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                Box(modifier = Modifier.weight(1f)) {
                                    MainContentTabs(
                                        currentTab = currentTab,
                                        viewModel = storeViewModel,
                                        onAppClick = { selectedAppPackageState.value = it },
                                        onNavigateToDiscover = { currentTabState.value = 0 }
                                    )
                                }
                            }
                        } else {
                            // Standard Bottom navigation for phones
                            Scaffold(
                                bottomBar = {
                                    NavigationBar(
                                        modifier = Modifier.testTag("bottom_nav_bar")
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == 0,
                                            onClick = { currentTabState.value = 0 },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == 0) Icons.Filled.Explore else Icons.Outlined.Explore,
                                                    contentDescription = "Discover"
                                                )
                                            },
                                            label = { Text("Discover") },
                                            modifier = Modifier.testTag("nav_tab_discover")
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == 1,
                                            onClick = { currentTabState.value = 1 },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == 1) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                                                    contentDescription = "Library"
                                                )
                                            },
                                            label = { Text("Library") },
                                            modifier = Modifier.testTag("nav_tab_library")
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == 2,
                                            onClick = { currentTabState.value = 2 },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == 2) Icons.Filled.Dns else Icons.Outlined.Dns,
                                                    contentDescription = "Repositories"
                                                )
                                            },
                                            label = { Text("Repositories") },
                                            modifier = Modifier.testTag("nav_tab_repositories")
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    MainContentTabs(
                                        currentTab = currentTab,
                                        viewModel = storeViewModel,
                                        onAppClick = { selectedAppPackageState.value = it },
                                        onNavigateToDiscover = { currentTabState.value = 0 }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContentTabs(
    currentTab: Int,
    viewModel: StoreViewModel,
    onAppClick: (String) -> Unit,
    onNavigateToDiscover: () -> Unit
) {
    when (currentTab) {
        0 -> DiscoverScreen(
            viewModel = viewModel,
            onAppClick = onAppClick
        )
        1 -> DownloadsScreen(
            viewModel = viewModel,
            onAppClick = onAppClick,
            onNavigateToDiscover = onNavigateToDiscover
        )
        2 -> RepoManagerScreen(
            viewModel = viewModel
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
