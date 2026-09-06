package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CheckStatus
import com.example.data.model.FullAuditReport
import com.example.data.model.ParameterDiagnostic
import com.example.data.model.SecurityCheckItem
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelCyan
import com.example.ui.theme.SentinelSurface
import com.example.ui.theme.SentinelSurfaceVariant
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusFailBg
import com.example.ui.theme.StatusPass
import com.example.ui.theme.StatusPassBg
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.StatusWarnBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DiagnosticDetailDialog(
    diagnostic: ParameterDiagnostic,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val (statusColor, statusBg, statusTitle, statusIcon) = when (diagnostic.status) {
        CheckStatus.PASS -> Quad(StatusPass, StatusPassBg, "LOLOS VERIFIKASI", Icons.Default.CheckCircle)
        CheckStatus.WARN -> Quad(StatusWarn, StatusWarnBg, "PERINGATAN WASPADA", Icons.Default.WarningAmber)
        CheckStatus.FAIL -> Quad(StatusFail, StatusFailBg, "TERDETEKSI BOCOR", Icons.Default.ErrorOutline)
        CheckStatus.PENDING -> Quad(TextMuted, Color(0x2064748B), "MENUNGGU FULL AUDIT", Icons.Default.Info)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(statusBg)
                                .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = diagnostic.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kunci: ${diagnostic.key}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Badge Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS AUDIT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    StatusPill(status = diagnostic.status, customLabel = statusTitle)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Current Value Box
                    item {
                        Text(
                            text = "NILAI PARAMETER SAAT INI",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF070B12))
                                .border(1.dp, SentinelCardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = diagnostic.value.ifEmpty { "<Kosong / Tidak Ada>" },
                                    color = SentinelCyan,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(diagnostic.title, diagnostic.value)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "${diagnostic.title} disalin", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin Nilai",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Target Tracking Analysis
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = null,
                                tint = SentinelCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ANALISIS PELACAKAN SISTEM TARGET",
                                color = SentinelCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SentinelSurface)
                                .border(1.dp, SentinelCardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = diagnostic.trackingAnalysis,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Fix & Remediation Guide
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = StatusWarn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PANDUAN PERBAIKAN / CARA FIX",
                                color = StatusWarn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E2430))
                                .border(1.dp, StatusWarn.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = diagnostic.fixGuide.ifEmpty { "Tidak ada rekomendasi perbaikan khusus untuk parameter ini." },
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Panduan Fix: ${diagnostic.title}", diagnostic.fixGuide)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Panduan perbaikan disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = StatusWarn
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Salin Panduan Fix", fontSize = 11.sp, color = StatusWarn)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SentinelCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup Pemeriksaan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SecurityCheckDetailDialog(
    item: SecurityCheckItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val (statusColor, statusBg, statusTitle, statusIcon) = when (item.status) {
        CheckStatus.PASS -> Quad(StatusPass, StatusPassBg, "LOLOS AUDIT", Icons.Default.CheckCircle)
        CheckStatus.WARN -> Quad(StatusWarn, StatusWarnBg, "PERINGATAN WASPADA", Icons.Default.WarningAmber)
        CheckStatus.FAIL -> Quad(StatusFail, StatusFailBg, "TERDETEKSI BOCOR", Icons.Default.ErrorOutline)
        CheckStatus.PENDING -> Quad(TextMuted, Color(0x2064748B), "MENUNGGU AUDIT", Icons.Default.Info)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(statusBg)
                                .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kategori: ${item.category.title}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS SISTEM",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    StatusPill(status = item.status, customLabel = statusTitle)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Details
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Hasil Temuan / Detection Summary
                    item {
                        Text(
                            text = "RINGKASAN HASIL AUDIT",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SentinelSurface)
                                .border(1.dp, SentinelCardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.detail,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Technical Diagnostic Log
                    if (item.technicalLog.isNotEmpty()) {
                        item {
                            Text(
                                text = "LOG DIAGNOSTIK TEKNIS",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF070B12))
                                    .border(1.dp, SentinelCardBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = item.technicalLog,
                                    color = SentinelCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Remediation & Fix Guide
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = StatusWarn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PANDUAN PERBAIKAN / CARA BENAHI",
                                color = StatusWarn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E2430))
                                .border(1.dp, StatusWarn.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = item.fixGuide.ifEmpty { "Periksa modul root hide dan pastikan aplikasi target masuk ke dalam Zygisk DenyList / Shamiko." },
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Panduan Fix: ${item.title}", item.fixGuide)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Panduan perbaikan disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = StatusWarn
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Salin Panduan Fix", fontSize = 11.sp, color = StatusWarn)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SentinelCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Inspection Modal triggered when clicking a status summary chip (Lolos / Waspada / Bocor)
 */
@Composable
fun StatusInspectionModal(
    status: CheckStatus,
    report: FullAuditReport,
    onDismiss: () -> Unit,
    onSelectDiagnostic: (ParameterDiagnostic) -> Unit,
    onSelectCheckItem: (SecurityCheckItem) -> Unit
) {
    val (statusColor, statusBg, statusTitle, statusIcon) = when (status) {
        CheckStatus.PASS -> Quad(StatusPass, StatusPassBg, "Lolos Audit", Icons.Default.CheckCircle)
        CheckStatus.WARN -> Quad(StatusWarn, StatusWarnBg, "Waspada (Perlu Dibenahi)", Icons.Default.WarningAmber)
        CheckStatus.FAIL -> Quad(StatusFail, StatusFailBg, "Bocor / Terdeteksi (Kritis)", Icons.Default.ErrorOutline)
        CheckStatus.PENDING -> Quad(TextMuted, Color(0x2064748B), "Menunggu Full Audit", Icons.Default.Info)
    }

    val matchingChecks: List<SecurityCheckItem> = report.securityChecks.filter { it.status == status }
    val matchingDiagnostics: List<ParameterDiagnostic> = report.identity.diagnostics.values.filter { it.status == status }

    var selectedTab by remember { mutableStateOf("all") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            color = Color(0xFF0B1120)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(statusBg)
                                .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pelacakan Status $statusTitle",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total ${matchingChecks.size + matchingDiagnostics.size} item teridentifikasi",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == "all",
                        onClick = { selectedTab = "all" },
                        label = { Text("Semua (${matchingChecks.size + matchingDiagnostics.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor,
                            selectedLabelColor = Color.Black,
                            containerColor = SentinelSurface,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedTab == "checks",
                        onClick = { selectedTab = "checks" },
                        label = { Text("Shield / Root (${matchingChecks.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor,
                            selectedLabelColor = Color.Black,
                            containerColor = SentinelSurface,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedTab == "params",
                        onClick = { selectedTab = "params" },
                        label = { Text("Parameter (${matchingDiagnostics.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor,
                            selectedLabelColor = Color.Black,
                            containerColor = SentinelSurface,
                            labelColor = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (matchingChecks.isEmpty() && matchingDiagnostics.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada item dengan status ini.",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Security Checks Section
                    if (selectedTab != "params" && matchingChecks.isNotEmpty()) {
                        item {
                            Text(
                                text = "ITEM SHIELD & ROOT (${matchingChecks.size})",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(matchingChecks) { check ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SentinelSurface)
                                    .border(1.dp, SentinelCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { onSelectCheckItem(check) }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = check.title,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPill(status = check.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = check.detail,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    if (check.fixGuide.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                tint = StatusWarn,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Ketuk untuk melihat panduan fix",
                                                color = StatusWarn,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Device Parameters Section
                    if (selectedTab != "checks" && matchingDiagnostics.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PARAMETER IDENTITAS (${matchingDiagnostics.size})",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(matchingDiagnostics) { diag ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SentinelSurface)
                                    .border(1.dp, SentinelCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { onSelectDiagnostic(diag) }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = diag.title,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPill(status = diag.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nilai: ${diag.value.ifEmpty { "<Kosong>" }}",
                                        color = SentinelCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = diag.trackingAnalysis,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = StatusWarn,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Ketuk untuk melihat panduan fix",
                                            color = StatusWarn,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SentinelSurfaceVariant,
                        contentColor = SentinelCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
