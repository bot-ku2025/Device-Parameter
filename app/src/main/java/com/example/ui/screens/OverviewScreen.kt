package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckStatus
import com.example.data.model.DeviceHardwareStats
import com.example.data.model.FullAuditReport
import com.example.data.model.ParameterDiagnostic
import com.example.data.model.SecurityCheckItem
import com.example.ui.components.CircularGauge
import com.example.ui.components.DiagnosticDetailDialog
import com.example.ui.components.SecurityCheckDetailDialog
import com.example.ui.components.StatusInspectionModal
import com.example.ui.components.StatusPill
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
import com.example.ui.viewmodel.AuditScanState

@Composable
fun OverviewScreen(
    hardwareStats: DeviceHardwareStats,
    auditState: AuditScanState,
    latestReport: FullAuditReport?,
    anomalyAlertsEnabled: Boolean,
    onStartAudit: () -> Unit,
    onToggleAnomalyAlerts: () -> Unit,
    onNavigateToShield: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var selectedInspectionStatus by remember { mutableStateOf<CheckStatus?>(null) }
    var selectedDiagnostic by remember { mutableStateOf<ParameterDiagnostic?>(null) }
    var selectedCheckItem by remember { mutableStateOf<SecurityCheckItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SentinelDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MONITOR & AUDIT REALTIME",
                    color = SentinelCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "Dasbor Parameter Perangkat",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            StatusPill(
                status = if (latestReport != null) CheckStatus.PASS else CheckStatus.PENDING,
                customLabel = if (latestReport != null) "Diaudit" else "Perlu Audit"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Realtime Hardware Analytics Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SentinelSurface)
                .border(1.dp, SentinelCardBorder, RoundedCornerShape(16.dp))
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
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Hardware Stats",
                            tint = SentinelCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Beban Kinerja Realtime",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusPass)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE",
                            color = StatusPass,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gauges Row (CPU & RAM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularGauge(
                        value = hardwareStats.cpuUsagePercent,
                        label = "CPU (${hardwareStats.cpuCores} Core)",
                        size = 118.dp,
                        strokeWidth = 9.dp
                    )

                    CircularGauge(
                        value = hardwareStats.ramUsagePercent,
                        label = "RAM (${hardwareStats.ramUsedMb} MB)",
                        size = 118.dp,
                        strokeWidth = 9.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network & System Details Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SentinelSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Jaringan",
                            tint = SentinelBlueLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = hardwareStats.networkType,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "IP: ${hardwareStats.ipAddress}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = hardwareStats.wifiSsid,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (hardwareStats.hasVpn) {
                            Text(
                                text = "VPN AKTIF",
                                color = StatusWarn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Audit CTA & Scanning State Card
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
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Full Audit Device Parameter",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Lakukan audit menyeluruh untuk memverifikasi kebocoran root, Play Integrity, dan akurasi spoofing.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SentinelCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (auditState) {
                    is AuditScanState.Idle -> {
                        Button(
                            onClick = onStartAudit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SentinelCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_audit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mulai Full Audit Sekarang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    is AuditScanState.Scanning -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = auditState.currentStage,
                                    color = SentinelCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${(auditState.progress * 100).toInt()}%",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { auditState.progress },
                                color = SentinelCyan,
                                trackColor = SentinelSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                    is AuditScanState.Completed -> {
                        Button(
                            onClick = onStartAudit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SentinelSurfaceVariant,
                                contentColor = SentinelCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, SentinelCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .testTag("rescan_audit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jalankan Ulang Full Audit",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Section (Only shown after Full Audit)
        if (latestReport != null) {
            val score = latestReport.score
            val scoreColor = when {
                score.overallScore >= 85 -> StatusPass
                score.overallScore >= 60 -> StatusWarn
                else -> StatusFail
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SentinelSurface)
                    .border(1.dp, scoreColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Skor Ketahanan Siluman",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = score.stealthLevel,
                                color = scoreColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        CircularGauge(
                            value = score.overallScore.toFloat(),
                            label = "Skor",
                            size = 85.dp,
                            strokeWidth = 7.dp,
                            isScoreMode = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Breakdown Chips (Interactive for diagnostic tracking & fix)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusPill(
                            status = CheckStatus.PASS,
                            customLabel = "${score.passedCount} Lolos",
                            onClick = { selectedInspectionStatus = CheckStatus.PASS }
                        )
                        StatusPill(
                            status = CheckStatus.WARN,
                            customLabel = "${score.warnCount} Waspada",
                            onClick = { selectedInspectionStatus = CheckStatus.WARN }
                        )
                        StatusPill(
                            status = CheckStatus.FAIL,
                            customLabel = "${score.failCount} Bocor",
                            onClick = { selectedInspectionStatus = CheckStatus.FAIL }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Ketuk salah satu indikator di atas untuk melacak & melihat cara fix",
                        color = SentinelCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = score.riskSummary,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    if (score.warnCount > 0 || score.failCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x18FFB300))
                                .border(1.dp, StatusWarn.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = StatusWarn,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Item Waspada Perlu Dibenahi (${score.warnCount})",
                                        color = StatusWarn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Lihat Semua >",
                                    color = SentinelCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { selectedInspectionStatus = CheckStatus.WARN }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val warnChecks = latestReport.securityChecks.filter { it.status == CheckStatus.WARN }
                            val warnParams = latestReport.identity.diagnostics.values.filter { it.status == CheckStatus.WARN }

                            warnChecks.forEach { check ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedCheckItem = check }
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = check.title,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = check.detail,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cara Fix >",
                                        color = StatusWarn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            warnParams.forEach { diag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedDiagnostic = diag }
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = diag.title,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${diag.key}: ${diag.value}",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cara Fix >",
                                        color = StatusWarn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick link to Shield Screen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1000E5FF))
                            .border(0.8.dp, SentinelCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Buka detail deteksi root & Play Integrity",
                            color = SentinelCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = onNavigateToShield,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SentinelCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("view_shield_details_button")
                        ) {
                            Text("Buka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Notice card indicating results are gated behind Full Audit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SentinelSurface)
                    .border(1.dp, SentinelCardBorder, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x1500E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SentinelCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Hasil Menunggu Full Audit",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Semua hasil verifikasi parameter spoofing, evaluasi biner root, status kernel, dan sertifikasi Google Play Integrity memerlukan Full Audit untuk memastikan keakuratan mutlak.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Anomaly Notification Configuration Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SentinelSurface)
                .border(1.dp, SentinelCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifikasi Instan Anomali",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Peringatan saat beban CPU > 88% atau RAM > 90%",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = anomalyAlertsEnabled,
                onCheckedChange = { onToggleAnomalyAlerts() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = SentinelCyan,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SentinelSurfaceVariant
                )
            )
        }
    }

    // Modal for inspecting all items in a status category (Lolos, Waspada, Bocor)
    if (selectedInspectionStatus != null && latestReport != null) {
        StatusInspectionModal(
            status = selectedInspectionStatus!!,
            report = latestReport,
            onDismiss = { selectedInspectionStatus = null },
            onSelectDiagnostic = { diag ->
                selectedDiagnostic = diag
            },
            onSelectCheckItem = { check ->
                selectedCheckItem = check
            }
        )
    }

    // Individual Parameter Diagnostic Dialog
    if (selectedDiagnostic != null) {
        DiagnosticDetailDialog(
            diagnostic = selectedDiagnostic!!,
            onDismiss = { selectedDiagnostic = null }
        )
    }

    // Individual Security Check Item Dialog
    if (selectedCheckItem != null) {
        SecurityCheckDetailDialog(
            item = selectedCheckItem!!,
            onDismiss = { selectedCheckItem = null }
        )
    }
}
