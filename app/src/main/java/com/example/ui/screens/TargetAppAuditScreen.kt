package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.util.AppIconManager

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
    onResetAudit: () -> Unit,
    onRefreshApps: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppTypeFilter.USER) }

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
            NetworkIntelligenceCard(
                networkIntel = latestSession?.networkIntel ?: LiveNetworkIntelligence(),
                isRunning = targetAuditState is TargetAuditState.Running
            )
        }

        item {
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
                        text = "Pilih aplikasi target untuk audit celah anti-fraud spesifik",
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

        // Search and Filter Bar
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
                        placeholder = { Text("Cari Shopee, Tokopedia, PineDrama, TikTok...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SentinelCyan, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
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

        // Action Buttons Row (Audit Button & Reset Button)
        item {
            val isScanning = targetAuditState is TargetAuditState.Running
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunAudit,
                    enabled = selectedPackages.isNotEmpty() && !isScanning,
                    modifier = Modifier
                        .weight(1f)
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (targetAuditState as TargetAuditState.Running).stage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    } else {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UJI TARGET (${selectedPackages.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reset Button
                if (latestSession != null) {
                    OutlinedButton(
                        onClick = onResetAudit,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("reset_target_audit_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StatusFail.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusFail)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Hasil Cek", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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

        // Result Section (Tampil Jika Ada Hasil Sesi)
        if (latestSession != null && latestSession.auditedApps.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HASIL AUDIT TARGET SPESIFIK",
                            color = SentinelCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Klik kartu untuk melihat detail bahaya & panduan fix",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (latestSession.overallReadinessScore >= 80) StatusPass.copy(alpha = 0.15f) else StatusFail.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Rata-rata: ${latestSession.overallReadinessScore}/100",
                                color = if (latestSession.overallReadinessScore >= 80) StatusPass else StatusFail,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Tombol Cepat Reset Hasil
                        IconButton(
                            onClick = onResetAudit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Tutup Hasil", tint = StatusFail, modifier = Modifier.size(18.dp))
                        }
                    }
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

        // Apps List
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
                            text = if (networkIntel.isVpnOrProxy) "Auto-Banned Anti-Fraud" else "Rotasi via Mode Pesawat",
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
    val context = LocalContext.current
    val appIcon = remember(app.packageName) {
        AppIconManager.getAppIcon(context, app.packageName)
    }

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
                // Real App Icon with Fallback
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SentinelDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (appIcon != null) {
                        Image(
                            bitmap = appIcon,
                            contentDescription = app.appName,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Icon(
                            imageVector = if (app.isRiskTarget) Icons.Default.Security else Icons.Default.Apps,
                            contentDescription = null,
                            tint = if (app.isRiskTarget) SentinelCyan else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
    // Mode Expand default Collapsed (false) agar tidak acak-acakan di layar
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appIcon = remember(result.packageName) {
        AppIconManager.getAppIcon(context, result.packageName)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelSurface),
        border = BorderStroke(1.2.dp, Color(result.statusColorHex).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Real App Icon, App Name, Score Badge, Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SentinelDarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIcon != null) {
                            Image(
                                bitmap = appIcon,
                                contentDescription = result.appName,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = SentinelCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = result.appName,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = result.packageName,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "Profil: ${result.engineType.displayName}",
                            color = SentinelCyan,
                            fontSize = 10.sp
                        )
                    }
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

            // Context Tag (Fokus Sasaran Cek)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SentinelDarkBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Fokus Audit: ${result.engineType.riskContext}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

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

            Spacer(modifier = Modifier.height(6.dp))

            // Toggle Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Tutup Rincian Detail" else "Klik untuk Buka Detail Bahaya & Solusi Fix",
                    color = if (isExpanded) SentinelCyan else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isExpanded) SentinelCyan else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Expandable Detail Area
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.multiAccountAdvice,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

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
                            text = "Panduan Fix Tuntas Multi-Root",
                            color = SentinelCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        result.fixSteps.forEach { step ->
                            MultiRootFixStepBox(step)
                            Spacer(modifier = Modifier.height(8.dp))
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
                                text = "Semua parameter aman! Tidak ada celah biner root, mount leak, atau folder blacklist yang terbaca oleh ${result.appName}.",
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
fun MultiRootFixStepBox(step: AppFixStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelDarkBg),
        border = BorderStroke(0.8.dp, SentinelCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Action Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
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

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = step.actionTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = step.detailedInstruction,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-blok 1: Menggunakan KernelSU / ReSuKSU / KSU Next
            RootSolutionSubBox(
                badgeLabel = "Jika Menggunakan KSU / ReSuKSU / KSU Next",
                badgeColor = Color(0xFF00E676),
                fixText = step.ksuFix
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-blok 2: Menggunakan Magisk / Alpha
            RootSolutionSubBox(
                badgeLabel = "Jika Menggunakan Magisk / Alpha",
                badgeColor = Color(0xFFFF9100),
                fixText = step.magiskFix
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-blok 3: Menggunakan APatch
            RootSolutionSubBox(
                badgeLabel = "Jika Menggunakan APatch",
                badgeColor = Color(0xFF2979FF),
                fixText = step.apatchFix
            )

            if (step.generalAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                RootSolutionSubBox(
                    badgeLabel = "Tindakan Bersih / Storage",
                    badgeColor = Color(0xFFE040FB),
                    fixText = step.generalAction
                )
            }
        }
    }
}

@Composable
fun RootSolutionSubBox(badgeLabel: String, badgeColor: Color, fixText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SentinelSurface)
            .border(0.6.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeLabel,
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fixText,
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
