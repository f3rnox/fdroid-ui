package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AppDetailsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.RepoManagerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StoreViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("store_prefs", MODE_PRIVATE)
            }
            var darkThemeSetting by remember {
                mutableIntStateOf(sharedPrefs.getInt("dark_theme_mode", 0)) // 0 = System, 1 = Light, 2 = Dark
            }

            // Real-time listener for theme preferences 변경
            DisposableEffect(sharedPrefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_theme_mode") {
                        darkThemeSetting = sharedPrefs.getInt("dark_theme_mode", 0)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val systemTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val useDarkTheme = when (darkThemeSetting) {
                1 -> false
                2 -> true
                else -> systemTheme
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val storeViewModel: StoreViewModel = viewModel()
                val currentTabState = remember { mutableStateOf(0) } // 0 = Discover, 1 = Library/Downloads, 2 = Repositories, 3 = Settings
                val selectedAppPackageState = remember { mutableStateOf<String?>(null) }

                val currentTab = currentTabState.value
                val selectedAppPackage = selectedAppPackageState.value

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val triggerMenuOpen: () -> Unit = {
                    scope.launch {
                        drawerState.open()
                    }
                }

                // Intercept back button when inspecting details
                if (selectedAppPackage != null) {
                    BackHandler {
                        selectedAppPackageState.value = null
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Pulse Droid",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            Text(
                                text = "F-Droid Client Store",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            listOf(
                                Triple("Discover", Icons.Filled.Explore, 0),
                                Triple("Library", Icons.Filled.FolderSpecial, 1),
                                Triple("Repositories", Icons.Filled.Dns, 2),
                                Triple("Settings", Icons.Filled.Settings, 3)
                            ).forEach { (label, icon, index) ->
                                val isSelected = currentTab == index
                                NavigationDrawerItem(
                                    label = { Text(label, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(icon, contentDescription = label) },
                                    selected = isSelected,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        currentTabState.value = index
                                    },
                                    modifier = Modifier
                                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                                        .testTag("drawer_item_${label.lowercase()}"),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                ) {
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
                                        Spacer(modifier = Modifier.height(12.dp))
                                        NavigationRailItem(
                                            selected = currentTab == 3,
                                            onClick = { currentTabState.value = 3 },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                                    contentDescription = "Settings"
                                                )
                                            },
                                            label = { Text("Settings") },
                                            modifier = Modifier.testTag("rail_tab_settings")
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                    }

                                    VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                    Box(modifier = Modifier.weight(1f)) {
                                        MainContentTabs(
                                            currentTab = currentTab,
                                            viewModel = storeViewModel,
                                            onAppClick = { selectedAppPackageState.value = it },
                                            onNavigateToDiscover = { currentTabState.value = 0 },
                                            onMenuClick = triggerMenuOpen
                                        )
                                    }
                                }
                            } else {
                                // Standard Bottom navigation for phones
                                Scaffold(
                                    bottomBar = {
                                        // Bottom navigation bar shows three main sections
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
                                            onNavigateToDiscover = { currentTabState.value = 0 },
                                            onMenuClick = triggerMenuOpen
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
}

@Composable
fun MainContentTabs(
    currentTab: Int,
    viewModel: StoreViewModel,
    onAppClick: (String) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onMenuClick: () -> Unit
) {
    when (currentTab) {
        0 -> DiscoverScreen(
            viewModel = viewModel,
            onAppClick = onAppClick,
            onMenuClick = onMenuClick
        )
        1 -> DownloadsScreen(
            viewModel = viewModel,
            onAppClick = onAppClick,
            onNavigateToDiscover = onNavigateToDiscover,
            onMenuClick = onMenuClick
        )
        2 -> RepoManagerScreen(
            viewModel = viewModel,
            onMenuClick = onMenuClick
        )
        3 -> SettingsScreen(
            viewModel = viewModel,
            onMenuClick = onMenuClick
        )
    }
}
