package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DeviceParameterTopBar
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.IdentityScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.ShieldScreen
import com.example.ui.screens.TargetAppAuditScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SentinelCyan
import com.example.ui.theme.SentinelDarkBg
import com.example.ui.theme.SentinelSurface
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.SentinelViewModel

enum class DeviceParameterTab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    OVERVIEW("Dasbor", Icons.Default.Assessment, Icons.Outlined.Assessment),
    SHIELD("Shield", Icons.Default.Security, Icons.Outlined.Security),
    TARGET("Target App", Icons.Default.Apps, Icons.Outlined.Apps),
    IDENTITY("Parameter", Icons.Default.Badge, Icons.Outlined.Badge),
    HISTORY("Riwayat", Icons.Default.History, Icons.Outlined.History)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DeviceParameterApp()
            }
        }
    }
}

@Composable
fun DeviceParameterApp(viewModel: SentinelViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(DeviceParameterTab.OVERVIEW) }

    val hardwareStats by viewModel.hardwareStats.collectAsStateWithLifecycle()
    val identity by viewModel.identity.collectAsStateWithLifecycle()
    val auditState by viewModel.auditState.collectAsStateWithLifecycle()
    val latestReport by viewModel.latestReport.collectAsStateWithLifecycle()
    val anomalyAlertsEnabled by viewModel.anomalyAlertsEnabled.collectAsStateWithLifecycle()
    val historyLogs by viewModel.historyLogs.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val selectedPackages by viewModel.selectedPackageNames.collectAsStateWithLifecycle()
    val targetAuditState by viewModel.targetAuditState.collectAsStateWithLifecycle()
    val latestTargetSession by viewModel.latestTargetSession.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = SentinelDarkBg,
        topBar = {
            DeviceParameterTopBar(
                anomalyAlertsEnabled = anomalyAlertsEnabled,
                onToggleAnomalyAlerts = { viewModel.toggleAnomalyAlerts() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SentinelSurface,
                contentColor = SentinelCyan,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .background(SentinelSurface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                DeviceParameterTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = SentinelCyan,
                            indicatorColor = SentinelCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                DeviceParameterTab.OVERVIEW -> {
                    OverviewScreen(
                        hardwareStats = hardwareStats,
                        auditState = auditState,
                        latestReport = latestReport,
                        anomalyAlertsEnabled = anomalyAlertsEnabled,
                        onStartAudit = { viewModel.startDeepAudit() },
                        onToggleAnomalyAlerts = { viewModel.toggleAnomalyAlerts() },
                        onNavigateToShield = { currentTab = DeviceParameterTab.SHIELD },
                        identity = identity
                    )
                }
                DeviceParameterTab.SHIELD -> {
                    ShieldScreen(
                        report = latestReport,
                        onTriggerAudit = { viewModel.startDeepAudit() }
                    )
                }
                DeviceParameterTab.TARGET -> {
                    TargetAppAuditScreen(
                        installedApps = installedApps,
                        selectedPackages = selectedPackages,
                        targetAuditState = targetAuditState,
                        latestSession = latestTargetSession,
                        onToggleAppSelection = { pkg -> viewModel.toggleAppSelection(pkg) },
                        onSelectAllFiltered = { pkgs -> viewModel.selectAllFiltered(pkgs) },
                        onClearSelection = { viewModel.clearSelectedApps() },
                        onRunAudit = { viewModel.runTargetAppAudit() },
                        onResetAudit = { viewModel.resetTargetAuditSession() },
                        onRefreshApps = { viewModel.loadInstalledApps() }
                    )
                }
                DeviceParameterTab.IDENTITY -> {
                    IdentityScreen(
                        identity = identity,
                        latestReport = latestReport,
                        onTriggerAudit = { viewModel.startDeepAudit() },
                        onRandomizeSpoof = {
                            viewModel.startDeepAudit()
                        }
                    )
                }
                DeviceParameterTab.HISTORY -> {
                    HistoryScreen(
                        logs = historyLogs,
                        onDeleteScan = { id -> viewModel.deleteScan(id) },
                        onClearAll = { viewModel.clearAllHistory() }
                    )
                }
            }
        }
    }
}
