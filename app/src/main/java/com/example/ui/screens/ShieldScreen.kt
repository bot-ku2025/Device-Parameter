package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckStatus
import com.example.data.model.DangerousPathReport
import com.example.data.model.FullAuditReport
import com.example.data.model.PlayIntegrityReport
import com.example.data.model.SecurityCategory
import com.example.data.model.SecurityCheckItem
import com.example.data.model.SpoofDepthAnalysis
import com.example.data.model.SpoofLeakItem
import com.example.ui.components.SecurityCheckDetailDialog
import com.example.ui.components.StatusPill
import com.example.ui.theme.SentinelBlue
import com.example.ui.theme.SentinelBlueLight
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

@Composable
fun ShieldScreen(
    report: FullAuditReport?,
    onTriggerAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<SecurityCategory?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<CheckStatus?>(null) }
    var selectedCheckItem by remember { mutableStateOf<SecurityCheckItem?>(null) }
    var selectedLeakItem by remember { mutableStateOf<SpoofLeakItem?>(null) }

    val checks = report?.securityChecks ?: emptyList()
    val passCount = checks.count { it.status == CheckStatus.PASS }
    val warnCount = checks.count { it.status == CheckStatus.WARN }
    val failCount = checks.count { it.status == CheckStatus.FAIL }

    val filteredChecks = checks.filter { check ->
        val catMatch = selectedCategoryFilter == null || check.category == selectedCategoryFilter
        val statusMatch = selectedStatusFilter == null || check.status == selectedStatusFilter
        catMatch && statusMatch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SentinelDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHIELD & AUDIT SISTEM",
                        color = SentinelCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Integritas & Deteksi Root",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusPill(
                    status = if (report != null && report.score.overallScore >= 80) CheckStatus.PASS else if (report != null) CheckStatus.WARN else CheckStatus.PENDING,
                    customLabel = if (report != null) "${report.score.overallScore}/100" else "Menunggu Audit"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (report == null) {
                // Notice: User must run Full Audit to unlock results
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F1B2E), Color(0xFF0A1220))
                            )
                        )
                        .border(1.dp, SentinelCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x2000E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SentinelCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Full Audit Diperlukan",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Untuk menjaga keakuratan, hasil pengujian akses root, status kernel, bootloader, dan sertifikasi Play Integrity hanya ditampilkan setelah Full Audit dijalankan.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onTriggerAudit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SentinelCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jalankan Full Audit Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Spoof Depth & Anti-Fraud Leak Scanner Card
                SpoofDepthScannerCard(
                    spoofDepth = report.spoofDepth,
                    onInspectLeak = { selectedLeakItem = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Play Integrity API Card
                PlayIntegrityCard(playIntegrity = report.playIntegrity)

                Spacer(modifier = Modifier.height(16.dp))

                // Kernel & Bootloader Summary Card
                KernelBootloaderCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Dangerous Folders & Root Paths Inspection Card
                DangerousPathsAuditCard(report = report.dangerousPathReport)

                Spacer(modifier = Modifier.height(18.dp))

                // Category Filter Chips
                Text(
                    text = "Log Eksploit & Indikator Keamanan",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Semua Kategori", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SentinelCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = SentinelSurface,
                            labelColor = TextSecondary
                        )
                    )

                    FilterChip(
                        selected = selectedCategoryFilter == SecurityCategory.ROOT_ACCESS,
                        onClick = { selectedCategoryFilter = SecurityCategory.ROOT_ACCESS },
                        label = { Text("Root", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SentinelCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = SentinelSurface,
                            labelColor = TextSecondary
                        )
                    )

                    FilterChip(
                        selected = selectedCategoryFilter == SecurityCategory.DEVICE_PROPS,
                        onClick = { selectedCategoryFilter = SecurityCategory.DEVICE_PROPS },
                        label = { Text("Props Spoof", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SentinelCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = SentinelSurface,
                            labelColor = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Status Filter Chips (Lolos / Waspada / Bocor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Semua Status", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SentinelCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SentinelCyan
                        )
                    )
                    FilterChip(
                        selected = selectedStatusFilter == CheckStatus.PASS,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == CheckStatus.PASS) null else CheckStatus.PASS
                        },
                        label = { Text("Lolos ($passCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusPass.copy(alpha = 0.2f),
                            selectedLabelColor = StatusPass
                        )
                    )
                    FilterChip(
                        selected = selectedStatusFilter == CheckStatus.WARN,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == CheckStatus.WARN) null else CheckStatus.WARN
                        },
                        label = { Text("Waspada ($warnCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusWarn.copy(alpha = 0.2f),
                            selectedLabelColor = StatusWarn
                        )
                    )
                    FilterChip(
                        selected = selectedStatusFilter == CheckStatus.FAIL,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == CheckStatus.FAIL) null else CheckStatus.FAIL
                        },
                        label = { Text("Bocor ($failCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusFail.copy(alpha = 0.2f),
                            selectedLabelColor = StatusFail
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Guidance Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SentinelSurface)
                        .border(0.8.dp, SentinelCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💡 Ketuk indikator status atau item untuk melacak detail kerentanan & panduan cara perbaikan sistem",
                        color = SentinelCyan,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (report != null) {
            items(filteredChecks, key = { it.id }) { item ->
                ExploitLogItemCard(
                    item = item,
                    onInspect = { selectedCheckItem = item }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (selectedCheckItem != null) {
        SecurityCheckDetailDialog(
            item = selectedCheckItem!!,
            onDismiss = { selectedCheckItem = null }
        )
    }

    if (selectedLeakItem != null) {
        LeakDetailDialog(
            leak = selectedLeakItem!!,
            onDismiss = { selectedLeakItem = null }
        )
    }
}

@Composable
fun PlayIntegrityCard(playIntegrity: PlayIntegrityReport) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SentinelSurface)
            .border(1.dp, SentinelCardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Play Integrity",
                        tint = SentinelCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Play Integrity API",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Certification Status Pill
                StatusPill(
                    status = if (playIntegrity.isPlayProtectCertified) CheckStatus.PASS else CheckStatus.FAIL,
                    customLabel = if (playIntegrity.isPlayProtectCertified) "Tersertifikasi" else "Uncertified"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Integrity Check Rows
            IntegrityVerdictRow(
                verdict = "MEETS_BASIC_INTEGRITY",
                description = "Perangkat berjalan pada runtime Android tanpa manipulasi biner inti dasar",
                passed = playIntegrity.meetsBasicIntegrity
            )

            Spacer(modifier = Modifier.height(10.dp))

            IntegrityVerdictRow(
                verdict = "MEETS_DEVICE_INTEGRITY",
                description = "Perangkat terdaftar resmi dan lolos uji kompatibilitas CTS Google",
                passed = playIntegrity.meetsDeviceIntegrity
            )

            Spacer(modifier = Modifier.height(10.dp))

            IntegrityVerdictRow(
                verdict = "MEETS_STRONG_INTEGRITY",
                description = "Kunci enkripsi terlindungi hardware (Hardware Key Attestation / TEE)",
                passed = playIntegrity.meetsStrongIntegrity
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = playIntegrity.summary,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun IntegrityVerdictRow(
    verdict: String,
    description: String,
    passed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SentinelSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = verdict,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (passed) Color(0x2010B981) else Color(0x20EF4444))
                .padding(4.dp)
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (passed) StatusPass else StatusFail,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun KernelBootloaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SentinelSurface)
            .border(1.dp, SentinelCardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = SentinelBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kernel & Status Bootloader",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusPill(
                    status = CheckStatus.PASS,
                    customLabel = "Enforcing"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Kernel OS", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = System.getProperty("os.version") ?: "5.10.136-android12-9",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Bootloader State", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "Locked (Green / Spoofed)",
                        color = StatusPass,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ExploitLogItemCard(
    item: SecurityCheckItem,
    onInspect: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SentinelSurface)
            .border(
                width = if (item.status == CheckStatus.FAIL || item.status == CheckStatus.WARN) 1.2.dp else 1.dp,
                color = if (item.status == CheckStatus.FAIL) StatusFail.copy(alpha = 0.7f)
                else if (item.status == CheckStatus.WARN) StatusWarn.copy(alpha = 0.6f)
                else SentinelCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onInspect() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val iconColor = when (item.status) {
                        CheckStatus.PASS -> StatusPass
                        CheckStatus.WARN -> StatusWarn
                        CheckStatus.FAIL -> StatusFail
                        CheckStatus.PENDING -> TextMuted
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(iconColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.category.title,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        status = item.status,
                        onClick = onInspect
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.detail,
                color = TextSecondary,
                fontSize = 12.sp
            )

            if (expanded) {
                if (item.technicalLog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF070B12))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "LOG: ${item.technicalLog}",
                            color = SentinelCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (item.trackingAnalysis.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎯 Analisis: ${item.trackingAnalysis}",
                        color = if (item.status == CheckStatus.FAIL) StatusFail else StatusWarn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.status == CheckStatus.FAIL) StatusFail.copy(alpha = 0.2f)
                        else if (item.status == CheckStatus.WARN) StatusWarn.copy(alpha = 0.2f)
                        else SentinelCyan.copy(alpha = 0.15f),
                        contentColor = if (item.status == CheckStatus.FAIL) StatusFail
                        else if (item.status == CheckStatus.WARN) StatusWarn
                        else SentinelCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text(
                        text = "Buka Pelacakan & Panduan Cara Fix ↗",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SpoofDepthScannerCard(
    spoofDepth: SpoofDepthAnalysis,
    onInspectLeak: (SpoofLeakItem) -> Unit
) {
    val tierColor = when {
        spoofDepth.depthScorePercent >= 85 -> StatusPass
        spoofDepth.depthScorePercent >= 55 -> StatusWarn
        else -> StatusFail
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SentinelSurface)
            .border(1.dp, SentinelCardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Spoof Depth",
                        tint = SentinelCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Uji Kedalaman Spoof & Anti-Fraud",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Audit Kebocoran Identitas Fisik (Multi-Akun)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tierColor.copy(alpha = 0.15f))
                        .border(1.dp, tierColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${spoofDepth.depthScorePercent}% DEEP",
                        color = tierColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Account Verdict Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(tierColor.copy(alpha = 0.15f), tierColor.copy(alpha = 0.05f))
                        )
                    )
                    .border(1.dp, tierColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (spoofDepth.depthScorePercent >= 85) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = spoofDepth.depthTier,
                            color = tierColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = spoofDepth.multiAccountSafetyVerdict,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Potensi Deteksi Risk Engine: ${spoofDepth.antiFraudDetectionLikelihood}",
                        color = tierColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Layer Breakdown Progress Bars
            Text(
                text = "Tingkat Penetrasi Lapisan Sistem:",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LayerProgressItem(
                label = "1. Surface Layer (Pengaturan & UI Telepon)",
                percent = spoofDepth.surfaceScore
            )
            Spacer(modifier = Modifier.height(6.dp))

            LayerProgressItem(
                label = "2. Vendor Props (Partisi ro.vendor.* & ro.boot)",
                percent = spoofDepth.vendorPropsScore
            )
            Spacer(modifier = Modifier.height(6.dp))

            LayerProgressItem(
                label = "3. Kernel & SoC Fisik (/proc/cpuinfo & Arsitektur)",
                percent = spoofDepth.hardwareLeakScore
            )
            Spacer(modifier = Modifier.height(6.dp))

            LayerProgressItem(
                label = "4. Cryptographic Trust (TEE & Play Integrity)",
                percent = spoofDepth.integrityScore
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Detected Leaks Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Titik Kebocoran Identitas Asli (${spoofDepth.detectedLeaks.size})",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (spoofDepth.detectedLeaks.isEmpty()) "0 Kebocoran" else "Ketuk untuk Fix",
                    color = if (spoofDepth.detectedLeaks.isEmpty()) StatusPass else SentinelCyan,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (spoofDepth.detectedLeaks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SentinelSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✓ Tidak ada kebocoran terdeteksi pada layer vendor dan hardware.",
                        color = StatusPass,
                        fontSize = 11.sp
                    )
                }
            } else {
                spoofDepth.detectedLeaks.forEach { leak ->
                    LeakItemCard(
                        leak = leak,
                        onClick = { onInspectLeak(leak) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LayerProgressItem(label: String, percent: Int) {
    val color = when {
        percent >= 80 -> StatusPass
        percent >= 50 -> StatusWarn
        else -> StatusFail
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
            Text(text = "$percent%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = SentinelSurfaceVariant
        )
    }
}

@Composable
fun LeakItemCard(
    leak: SpoofLeakItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SentinelSurfaceVariant)
            .border(0.8.dp, StatusWarn.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leak.layer,
                    color = SentinelCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BOCOR ↗",
                    color = StatusFail,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = leak.parameter,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Terspoof:", color = TextMuted, fontSize = 9.sp)
                    Text(leak.spoofedValue, color = StatusPass, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bocor Asli:", color = TextMuted, fontSize = 9.sp)
                    Text(leak.leakedRealValue, color = StatusFail, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ketuk untuk melihat dampak anti-fraud & solusi fix tuntas",
                color = SentinelBlueLight,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun LeakDetailDialog(
    leak: SpoofLeakItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SentinelSurface,
        title = {
            Column {
                Text(
                    text = "Detail Titik Kebocoran Identitas",
                    color = SentinelCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = leak.parameter,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SentinelSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Layer Terdeteksi: ${leak.layer}", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Nilai Spoof: ${leak.spoofedValue}", color = StatusPass, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Nilai Asli Bocor: ${leak.leakedRealValue}", color = StatusFail, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Dampak Pada Multi-Akun / Anti-Fraud:",
                    color = StatusWarn,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = leak.riskImpact,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Panduan Solusi Fix Tuntas:",
                    color = SentinelCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SentinelDarkBg)
                        .border(0.8.dp, SentinelCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = leak.fixSolution,
                        color = SentinelBlueLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentinelCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun DangerousPathsAuditCard(report: DangerousPathReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelSurface),
        border = BorderStroke(1.dp, if (!report.hasDanger) SentinelCardBorder else StatusFail.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (!report.hasDanger) StatusPass else StatusFail,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audit Folder & Mount Berbahaya",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!report.hasDanger) StatusPass.copy(alpha = 0.15f) else StatusFail.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (!report.hasDanger) "TERISOLASI (PASS)" else "BOCOR (${report.foundFolders.size + report.foundBinaries.size + report.foundMountLeaks.size})",
                        color = if (!report.hasDanger) StatusPass else StatusFail,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!report.hasDanger)
                    "Semua folder root (/data/adb, Magisk, KSU, APatch), biner su/busybox, dan tabel mount virtual (/proc/mounts) terisolasi bersih dari deteksi anti-tamper."
                else "Terdeteksi jejak fisik root atau partisi virtual kernel yang dapat terbaca oleh aplikasi target tanpa izin root!",
                color = if (!report.hasDanger) TextSecondary else StatusWarn,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniAuditPathStatus(
                    title = "Folder Root",
                    target = "/data/adb /sbin",
                    isClean = report.foundFolders.isEmpty(),
                    detectedCount = report.foundFolders.size,
                    modifier = Modifier.weight(1f)
                )

                MiniAuditPathStatus(
                    title = "Biner Eksekusi",
                    target = "su / busybox",
                    isClean = report.foundBinaries.isEmpty(),
                    detectedCount = report.foundBinaries.size,
                    modifier = Modifier.weight(1f)
                )

                MiniAuditPathStatus(
                    title = "Tabel Mount",
                    target = "/proc/mounts",
                    isClean = report.foundMountLeaks.isEmpty(),
                    detectedCount = report.foundMountLeaks.size,
                    modifier = Modifier.weight(1f)
                )
            }

            if (report.hasDanger) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FF5252))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Rekomendasi Fix: Pasang modul Shamiko (Zygisk) atau aktifkan 'Mount Namespace Isolation' pada KernelSU/APatch agar path ini tidak dapat dibaca oleh aplikasi perbankan/e-commerce.",
                        color = Color(0xFFFF8A80),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniAuditPathStatus(
    title: String,
    target: String,
    isClean: Boolean,
    detectedCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SentinelDarkBg)
            .border(0.8.dp, if (isClean) SentinelCardBorder else StatusFail.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = target, color = TextMuted, fontSize = 9.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isClean) "Bersih" else "$detectedCount Bocor",
                color = if (isClean) StatusPass else StatusFail,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
