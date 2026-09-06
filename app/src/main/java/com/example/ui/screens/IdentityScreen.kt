package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckStatus
import com.example.data.model.DeviceIdentity
import com.example.data.model.FullAuditReport
import com.example.data.model.ParameterDiagnostic
import com.example.ui.components.DiagnosticDetailDialog
import com.example.ui.components.IdentityFieldCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.SentinelBlue
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
fun IdentityScreen(
    identity: DeviceIdentity,
    latestReport: FullAuditReport?,
    onTriggerAudit: () -> Unit,
    onRandomizeSpoof: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAudited = latestReport != null
    val statuses = identity.parameterStatuses

    var activeDiagnostic by remember { mutableStateOf<ParameterDiagnostic?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<CheckStatus?>(null) }

    val passCount = remember(statuses) { statuses.values.count { it == CheckStatus.PASS } }
    val warnCount = remember(statuses) { statuses.values.count { it == CheckStatus.WARN } }
    val failCount = remember(statuses) { statuses.values.count { it == CheckStatus.FAIL } }

    val onParamClick: (String) -> Unit = { key ->
        activeDiagnostic = identity.diagnostics[key]
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SentinelDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PARAMETER SYSTEM CHECKER",
                        color = SentinelCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Identitas Perangkat & Spoof",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusPill(
                    status = if (isAudited) CheckStatus.PASS else CheckStatus.PENDING,
                    customLabel = if (isAudited) "Terverifikasi" else "Perlu Audit"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audit Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isAudited)
                            Brush.verticalGradient(listOf(Color(0xFF0F2432), Color(0xFF0A1622)))
                        else
                            Brush.verticalGradient(listOf(Color(0xFF161E2E), Color(0xFF0D131F)))
                    )
                    .border(
                        1.dp,
                        if (isAudited) StatusPass.copy(alpha = 0.4f) else SentinelCyan.copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isAudited) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isAudited) StatusPass else SentinelCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAudited) "Parameter Telah Diaudit (${latestReport.score.overallScore}/100)" else "Status Audit: Menunggu Full Audit",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAudited) "Indikator warna aktif: Hijau (Lolos), Kuning (Waspada), Merah (Bocor)" else "Lakukan Full Audit untuk memvalidasi keabsahan nilai spoofing",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onTriggerAudit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAudited) SentinelSurfaceVariant else SentinelCyan,
                            contentColor = if (isAudited) SentinelCyan else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("audit_parameter_button")
                    ) {
                        Text(
                            text = if (isAudited) "Ulang" else "Audit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Status Filter and Tracking Guide Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("Semua (${statuses.size})", fontSize = 11.sp) },
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

            // Guidance Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SentinelSurface)
                    .border(0.8.dp, SentinelCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "💡 Ketuk parameter atau indikator warna untuk membuka analisis pelacakan & panduan cara fix",
                    color = SentinelCyan,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Device Selector Cards (Brand, Model, Version)
            if (selectedStatusFilter == null || statuses["brand"] == selectedStatusFilter) {
                PrimarySelectorCard(
                    label = "Brand",
                    value = identity.brand,
                    icon = Icons.Default.CreditCard,
                    status = statuses["brand"] ?: CheckStatus.PENDING,
                    onInspect = { onParamClick("brand") }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (selectedStatusFilter == null || statuses["model"] == selectedStatusFilter) {
                PrimarySelectorCard(
                    label = "Model",
                    value = identity.model,
                    icon = Icons.Default.PhoneAndroid,
                    status = statuses["model"] ?: CheckStatus.PENDING,
                    onInspect = { onParamClick("model") }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (selectedStatusFilter == null || statuses["android_version"] == selectedStatusFilter) {
                PrimarySelectorCard(
                    label = "Android Version",
                    value = identity.androidVersion,
                    icon = Icons.Default.Android,
                    status = statuses["android_version"] ?: CheckStatus.PENDING,
                    onInspect = { onParamClick("android_version") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tech Model & Codename Section Header
            Text(
                text = "Spesifikasi Perangkat & Board",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedStatusFilter == null || statuses["tech_model"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Tech Model",
                    value = identity.techModel,
                    icon = Icons.Default.SettingsCell,
                    status = statuses["tech_model"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["tech_model"],
                    onInspect = { onParamClick("tech_model") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["codename"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Codename",
                    value = identity.codename,
                    icon = Icons.Default.Tag,
                    status = statuses["codename"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["codename"],
                    onInspect = { onParamClick("codename") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["board"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Board / Platform",
                    value = identity.boardPlatform,
                    icon = Icons.Default.Memory,
                    status = statuses["board"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["board"],
                    onInspect = { onParamClick("board") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Identifiers Section (Android ID, IMEI 1/2, Serial, GSF ID)
            Text(
                text = "Nomor Seri & Pengenal Unik",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedStatusFilter == null || statuses["android_id"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Android ID",
                    value = identity.androidId,
                    icon = Icons.Default.VpnKey,
                    status = statuses["android_id"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["android_id"],
                    onInspect = { onParamClick("android_id") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["imei1"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "IMEI 1",
                    value = identity.imei1,
                    icon = Icons.Default.GridOn,
                    status = statuses["imei1"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["imei1"],
                    onInspect = { onParamClick("imei1") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["imei2"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "IMEI 2",
                    value = identity.imei2,
                    icon = Icons.Default.GridOn,
                    status = statuses["imei2"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["imei2"],
                    onInspect = { onParamClick("imei2") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["serial"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Serial",
                    value = identity.serial,
                    icon = Icons.Default.PermDeviceInformation,
                    status = statuses["serial"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["serial"],
                    onInspect = { onParamClick("serial") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["fingerprint"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Fingerprint",
                    value = identity.fingerprint,
                    icon = Icons.Default.Security,
                    status = statuses["fingerprint"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["fingerprint"],
                    onInspect = { onParamClick("fingerprint") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["gsf_id"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "GSF ID",
                    value = identity.gsfId,
                    icon = Icons.Default.Cloud,
                    status = statuses["gsf_id"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["gsf_id"],
                    onInspect = { onParamClick("gsf_id") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Network & Hardware MACs (Screenshot 2)
            Text(
                text = "Alamat Jaringan & Identitas MAC",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedStatusFilter == null || statuses["wifi_mac"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "WiFi MAC",
                    value = identity.wifiMac,
                    icon = Icons.Default.Wifi,
                    status = statuses["wifi_mac"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["wifi_mac"],
                    onInspect = { onParamClick("wifi_mac") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["wifi_ssid"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Wi-Fi SSID",
                    value = identity.wifiSsid,
                    icon = Icons.Default.Wifi,
                    status = statuses["wifi_ssid"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["wifi_ssid"],
                    onInspect = { onParamClick("wifi_ssid") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["wifi_bssid"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Wi-Fi BSSID",
                    value = identity.wifiBssid,
                    icon = Icons.Default.Wifi,
                    status = statuses["wifi_bssid"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["wifi_bssid"],
                    onInspect = { onParamClick("wifi_bssid") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["bluetooth_mac"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Bluetooth MAC",
                    value = identity.bluetoothMac,
                    icon = Icons.Default.Bluetooth,
                    status = statuses["bluetooth_mac"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["bluetooth_mac"],
                    onInspect = { onParamClick("bluetooth_mac") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Ad IDs, DRM & System Profiles (Screenshot 2 & 3)
            Text(
                text = "Identifikasi Iklan & DRM",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedStatusFilter == null || statuses["ad_id"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Advertising ID",
                    value = identity.advertisingId,
                    icon = Icons.Default.Shop,
                    status = statuses["ad_id"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["ad_id"],
                    onInspect = { onParamClick("ad_id") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["app_set_id"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "App Set ID",
                    value = identity.appSetId,
                    icon = Icons.Default.Tag,
                    status = statuses["app_set_id"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["app_set_id"],
                    onInspect = { onParamClick("app_set_id") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["widevine_drm"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Widevine DRM ID",
                    value = identity.widevineDrmId,
                    icon = Icons.Default.Lock,
                    status = statuses["widevine_drm"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["widevine_drm"],
                    onInspect = { onParamClick("widevine_drm") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["user_agent"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "User Agent",
                    value = identity.userAgent,
                    icon = Icons.Default.Language,
                    status = statuses["user_agent"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["user_agent"],
                    onInspect = { onParamClick("user_agent") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Keyboards, IME & Nearby (Screenshot 3)
            Text(
                text = "Paket Sistem, Keyboard & Bluetooth Sekitar",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedStatusFilter == null || statuses["installer_package"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Installer Package",
                    value = identity.installerPackage,
                    icon = Icons.Default.Shop,
                    status = statuses["installer_package"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["installer_package"],
                    onInspect = { onParamClick("installer_package") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["hidden_keyboards"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Hidden Keyboard Packages",
                    value = identity.hiddenKeyboardPackages,
                    icon = Icons.Default.Keyboard,
                    status = statuses["hidden_keyboards"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["hidden_keyboards"],
                    onInspect = { onParamClick("hidden_keyboards") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["virtual_ime"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Virtual Default IME",
                    value = identity.virtualDefaultIme,
                    icon = Icons.Default.Input,
                    status = statuses["virtual_ime"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["virtual_ime"],
                    onInspect = { onParamClick("virtual_ime") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["bt_name"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Nearby Bluetooth Name",
                    value = identity.nearbyBtName,
                    icon = Icons.Default.BluetoothSearching,
                    status = statuses["bt_name"] ?: CheckStatus.PENDING,
                    diagnostic = identity.diagnostics["bt_name"],
                    onInspect = { onParamClick("bt_name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedStatusFilter == null || statuses["bt_addr"] == selectedStatusFilter) {
                IdentityFieldCard(
                    label = "Nearby Bluetooth Address",
                    value = identity.nearbyBtAddress,
                    icon = Icons.Default.Bluetooth,
                    status = statuses["bt_addr"] ?: CheckStatus.PENDING,
                    isMonospace = true,
                    diagnostic = identity.diagnostics["bt_addr"],
                    onInspect = { onParamClick("bt_addr") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Button(
                onClick = onRandomizeSpoof,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentinelCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("randomize_identity_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acak Profil Spoofing (Randomize)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Detail Investigation Dialog when tapping any parameter indicator or card
    if (activeDiagnostic != null) {
        DiagnosticDetailDialog(
            diagnostic = activeDiagnostic!!,
            onDismiss = { activeDiagnostic = null }
        )
    }
}

@Composable
fun PrimarySelectorCard(
    label: String,
    value: String,
    icon: ImageVector,
    status: CheckStatus = CheckStatus.PASS,
    onInspect: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SentinelSurface)
            .border(
                width = if (status == CheckStatus.FAIL || status == CheckStatus.WARN) 1.2.dp else 1.dp,
                color = if (status == CheckStatus.FAIL) StatusFail.copy(alpha = 0.7f)
                else if (status == CheckStatus.WARN) StatusWarn.copy(alpha = 0.6f)
                else SentinelCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                if (onInspect != null) onInspect()
                else Toast.makeText(context, "$label: $value", Toast.LENGTH_SHORT).show()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = SentinelCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = value.ifEmpty { "<Pilih $label>" },
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            StatusPill(
                status = status,
                onClick = onInspect
            )
        }
    }
}
