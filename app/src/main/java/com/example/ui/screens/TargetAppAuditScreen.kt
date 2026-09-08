package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppDangerItem
import com.example.data.model.AppFixStep
import com.example.data.model.AppTypeFilter
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LiveNetworkIntelligence
import com.example.data.model.MultiAppAuditSession
import com.example.data.model.TargetAppAuditResult
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelCyan
import com.example.ui.theme.SentinelDarkBg
import com.example.ui.theme.SentinelSurface
import com.example.ui.theme.SentinelSurfaceVariant
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusPass
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TargetAuditState

@Composable
fun TargetAppAuditScreen(
    installedApps: List<InstalledAppInfo>,
    selectedPackages: Set<String>,
    targetAuditState: TargetAuditState,
    latestSession: MultiAppAuditSession?,
    onToggleAppSelection: (String) -> Unit,
    onSelectAllFiltered: (List<String>) -> Unit,
    onClearSelection: () -> Unit,
    onRunAudit: () -> Unit,
    onRefreshApps: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppTypeFilter.USER) }

    // Filter apps
    val filteredApps = remember(installedApps, searchQuery, selectedFilter) {
        installedApps.filter { app ->
            val matchesType = when (selectedFilter) {
                AppTypeFilter.ALL -> true
                AppTypeFilter.USER -> !app.isSystemApp
                AppTypeFilter.SYSTEM -> app.isSystemApp
            }
            val matchesQuery = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            matchesType && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Live Cloud Network Intelligence Card
            NetworkIntelligenceCard(
                networkIntel = latestSession?.networkIntel ?: LiveNetworkIntelligence(),
                isRunning = targetAuditState is TargetAuditState.Running
            )
        }

        item {
            // Title & Action Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Target Engine & Multi-Akun",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pilih aplikasi target untuk cek kesiapan spoof & anti-fraud",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onRefreshApps) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Apps",
                        tint = SentinelCyan
                    )
                }
            }
        }

        // Search and Type Filter Tabs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SentinelSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SentinelCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_target_apps_field"),
                        placeholder = { Text("Cari Shopee, Tokopedia, TikTok, DANA...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SentinelCyan, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SentinelDarkBg,
                            unfocusedContainerColor = SentinelDarkBg,
                            focusedBorderColor = SentinelCyan,
                            unfocusedBorderColor = SentinelCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedFilter == AppTypeFilter.USER,
                            onClick = { selectedFilter = AppTypeFilter.USER },
                            label = { Text("Third-Party (${installedApps.count { !it.isSystemApp }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SentinelCyan.copy(alpha = 0.2f),
                                selectedLabelColor = SentinelCyan
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == AppTypeFilter.SYSTEM,
                            onClick = { selectedFilter = AppTypeFilter.SYSTEM },
                            label = { Text("Sistem (${installedApps.count { it.isSystemApp }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SentinelCyan.copy(alpha = 0.2f),
                                selectedLabelColor = SentinelCyan
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == AppTypeFilter.ALL,
                            onClick = { selectedFilter = AppTypeFilter.ALL },
                            label = { Text("Semua", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SentinelCyan.copy(alpha = 0.2f),
                                selectedLabelColor = SentinelCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedPackages.size} aplikasi dipilih",
                            color = SentinelCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Pilih Semua",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onSelectAllFiltered(filteredApps.map { it.packageName }) }
                                    .padding(4.dp)
                            )
                            Text(
                                text = "Batal Pilih",
                                color = StatusFail,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onClearSelection() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Primary Audit Execution Button & Running Banner
        item {
            val isScanning = targetAuditState is TargetAuditState.Running
            Button(
                onClick = onRunAudit,
                enabled = selectedPackages.isNotEmpty() && !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("run_target_audit_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentinelCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = SentinelSurfaceVariant,
                    disabledContentColor = TextMuted
                )
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = (targetAuditState as TargetAuditState.Running).stage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UJI CELAH MULTI-AKUN (${selectedPackages.size} TARGET)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (targetAuditState as TargetAuditState.Running).progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = SentinelCyan,
                    trackColor = SentinelSurface
                )
            }
        }

        // Section: Audit Results (If Completed)
        if (latestSession != null && latestSession.auditedApps.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HASIL AUDIT TARGET SPESIFIK",
                        color = SentinelCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rata-rata Skor: ${latestSession.overallReadinessScore}/100",
                        color = if (latestSession.overallReadinessScore >= 80) StatusPass else StatusFail,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(latestSession.auditedApps) { appResult ->
                TargetAppResultCard(appResult)
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "DAFTAR APLIKASI UNTUK DIPILIH ULANG",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section: Selectable App Items List
        items(filteredApps) { app ->
            SelectableAppCard(
                app = app,
                isSelected = selectedPackages.contains(app.packageName),
                onToggle = { onToggleAppSelection(app.packageName) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun NetworkIntelligenceCard(networkIntel: LiveNetworkIntelligence, isRunning: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelSurface),
        border = BorderStroke(1.dp, if (networkIntel.isVpnOrProxy) StatusFail.copy(alpha = 0.5f) else SentinelCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (networkIntel.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (networkIntel.isOnline) StatusPass else StatusFail,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Network & Anti-Fraud Intel",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (networkIntel.isOnline) StatusPass.copy(alpha = 0.15f) else StatusFail.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (networkIntel.isOnline) "ONLINE SYNCED" else "OFFLINE",
                        color = if (networkIntel.isOnline) StatusPass else StatusFail,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Public IP Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SentinelDarkBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Public IP Publik", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = networkIntel.publicIp,
                            color = SentinelCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = networkIntel.isp, color = TextSecondary, fontSize = 9.sp, maxLines = 1)
                    }
                }

                // Proxy / VPN Status
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SentinelDarkBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Status VPN / Proxy", color = TextMuted, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (networkIntel.isVpnOrProxy) Icons.Default.VpnKey else Icons.Default.Public,
                                contentDescription = null,
                                tint = if (networkIntel.isVpnOrProxy) StatusFail else StatusPass,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (networkIntel.isVpnOrProxy) "VPN AKTIF (BAHAYA)" else "RESIDENTIAL (AMAN)",
                                color = if (networkIntel.isVpnOrProxy) StatusFail else StatusPass,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (networkIntel.isVpnOrProxy) "Auto-Banned E-Commerce" else "Rotasi via Mode Pesawat",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Peringatan Cluster: ${networkIntel.subnetClusterRisk}",
                color = if (networkIntel.isVpnOrProxy) StatusFail else TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun SelectableAppCard(
    app: InstalledAppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SentinelSurfaceVariant else SentinelSurface),
        border = BorderStroke(1.dp, if (isSelected) SentinelCyan else SentinelCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (app.isRiskTarget) SentinelCyan.copy(alpha = 0.15f) else SentinelDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (app.isRiskTarget) Icons.Default.Security else Icons.Default.Apps,
                        contentDescription = null,
                        tint = if (app.isRiskTarget) SentinelCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (app.isRiskTarget) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusWarn.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "TARGET RAWAN",
                                    color = StatusWarn,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = app.packageName,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )

                    Text(
                        text = "Engine: ${app.categoryLabel} (v${app.versionName})",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SentinelCyan,
                    uncheckedColor = TextMuted,
                    checkmarkColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun TargetAppResultCard(result: TargetAppAuditResult) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelSurface),
        border = BorderStroke(1.2.dp, Color(result.statusColorHex).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: App Name, Score Badge, Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.appName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.packageName,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SDK Profil: ${result.engineType.displayName}",
                        color = SentinelCyan,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(result.statusColorHex).copy(alpha = 0.15f))
                            .border(1.dp, Color(result.statusColorHex), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SKOR: ${result.readinessScore}/100",
                            color = Color(result.statusColorHex),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verdict Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(result.statusColorHex).copy(alpha = 0.12f))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.readinessScore >= 80) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(result.statusColorHex),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = result.verdictTitle,
                        color = Color(result.statusColorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.multiAccountAdvice,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    // Dangers Section
                    if (result.detectedDangers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bahaya & Celah Terdeteksi (${result.detectedDangers.size})",
                            color = StatusFail,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        result.detectedDangers.forEach { danger ->
                            DangerItemBox(danger)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Fix Steps Section
                    if (result.fixSteps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Langkah Fix Tuntas Agar Siap Multi-Akun",
                            color = SentinelCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        result.fixSteps.forEach { step ->
                            FixStepBox(step)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusPass.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Semua parameter aman! Tidak ada celah fisik root, mount leak, atau folder blacklist yang terbaca oleh ${result.appName}.",
                                color = StatusPass,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DangerItemBox(danger: AppDangerItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SentinelDarkBg)
            .border(0.8.dp, StatusFail.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = danger.title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StatusFail.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = danger.severity.label,
                        color = StatusFail,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = danger.explanation,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bukti Teknis: ${danger.technicalProof}",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun FixStepBox(step: AppFixStep) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SentinelDarkBg)
            .border(0.8.dp, SentinelCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SentinelCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step.stepNumber}",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = step.actionTitle,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = step.detailedInstruction,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alat/Modul: ${step.recommendedModuleOrTool}",
                    color = SentinelCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
