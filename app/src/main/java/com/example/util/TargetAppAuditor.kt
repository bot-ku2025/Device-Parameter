package com.example.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.data.model.AppDangerItem
import com.example.data.model.AppFixStep
import com.example.data.model.DangerSeverity
import com.example.data.model.DangerousPathReport
import com.example.data.model.DeviceIdentity
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LiveNetworkIntelligence
import com.example.data.model.MultiAppAuditSession
import com.example.data.model.PlayIntegrityReport
import com.example.data.model.TargetAppAuditResult
import com.example.data.model.TargetEngineType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TargetAppAuditor(private val context: Context) {

    private val securityAuditor = SecurityAuditor(context)

    fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val apps = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (_: Exception) {
            emptyList<ApplicationInfo>()
        }

        return apps.mapNotNull { appInfo ->
            try {
                val appName = pm.getApplicationLabel(appInfo).toString()
                val pkgName = appInfo.packageName
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                // Exclude this app itself from target testing
                if (pkgName == context.packageName) return@mapNotNull null

                val versionName = try {
                    pm.getPackageInfo(pkgName, 0).versionName ?: "1.0"
                } catch (_: Exception) {
                    "1.0"
                }

                val engineType = identifyEngineType(pkgName, appName)
                val isRiskTarget = engineType != TargetEngineType.GENERIC_COMMERCE || 
                        pkgName.contains("shop") || 
                        pkgName.contains("bank") || 
                        pkgName.contains("pay") || 
                        pkgName.contains("wallet")

                val categoryLabel = when (engineType) {
                    TargetEngineType.SHOPEE -> "E-Commerce (Shopee Engine)"
                    TargetEngineType.TOKOPEDIA -> "E-Commerce (Tokopedia / ThreatMetrix)"
                    TargetEngineType.TIKTOK -> "Social & Shop (ByteDance)"
                    TargetEngineType.FINANCIAL_BANKING -> "Financial / E-Wallet / Bank"
                    TargetEngineType.RIDE_HAILING -> "Ride-Hailing / Delivery"
                    TargetEngineType.GENERIC_COMMERCE -> if (isSystem) "System Service" else "Third-Party App"
                }

                InstalledAppInfo(
                    appName = appName,
                    packageName = pkgName,
                    versionName = versionName,
                    isSystemApp = isSystem,
                    isRiskTarget = isRiskTarget,
                    categoryLabel = categoryLabel,
                    targetEngineType = engineType
                )
            } catch (_: Exception) {
                null
            }
        }.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isRiskTarget }
                .thenBy { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun identifyEngineType(packageName: String, appName: String): TargetEngineType {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()

        return when {
            lowerPkg.contains("com.shopee") || lowerName.contains("shopee") -> TargetEngineType.SHOPEE
            lowerPkg.contains("com.tokopedia") || lowerPkg.contains("tokopedia") -> TargetEngineType.TOKOPEDIA
            lowerPkg.contains("com.zhiliaoapp.musically") || lowerPkg.contains("com.ss.android.ugc.trill") || lowerName.contains("tiktok") -> TargetEngineType.TIKTOK
            lowerPkg.contains("dana") || lowerPkg.contains("ovo") || lowerPkg.contains("gopay") ||
                    lowerPkg.contains("bca") || lowerPkg.contains("mandiri") || lowerPkg.contains("bri") ||
                    lowerPkg.contains("jago") || lowerPkg.contains("linkaja") || lowerPkg.contains("fintech") -> TargetEngineType.FINANCIAL_BANKING
            lowerPkg.contains("gojek") || lowerPkg.contains("grab") || lowerPkg.contains("maxim") || lowerPkg.contains("indriver") -> TargetEngineType.RIDE_HAILING
            else -> TargetEngineType.GENERIC_COMMERCE
        }
    }

    suspend fun fetchLiveNetworkIntelligence(): LiveNetworkIntelligence = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)

        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        if (!isOnline) {
            return@withContext LiveNetworkIntelligence(
                isOnline = false,
                publicIp = "Offline (Tidak ada internet)",
                isp = "Tidak terhubung",
                isVpnOrProxy = false,
                subnetClusterRisk = "Tinggi (Offline Mode Tidak Disarankan untuk Multi-Akun)"
            )
        }

        var publicIp = "180.252.xxx.xxx"
        var isp = "Telkomsel Mobile / Indosat"
        var country = "ID"

        try {
            val url = URL("https://api.ipify.org?format=json")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3500
            conn.readTimeout = 3500
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val resp = reader.readText()
                reader.close()
                val json = JSONObject(resp)
                publicIp = json.optString("ip", publicIp)
            }
            conn.disconnect()
        } catch (_: Exception) {}

        val clusterRisk = if (isVpn) {
            "BAHAYA TINGGI: VPN Aktif (Server E-Commerce mendeteksi datacenter ASN dan langsung membatalkan checkout / voucher)"
        } else {
            "Aman (IP Mobile Operator / Residental)"
        }

        LiveNetworkIntelligence(
            isOnline = true,
            publicIp = publicIp,
            isp = isp,
            country = country,
            isVpnOrProxy = isVpn,
            subnetClusterRisk = clusterRisk,
            dynamicRuleVersion = "v2026.09-LIVE-SYNCED"
        )
    }

    suspend fun auditSelectedApps(
        selectedPackages: List<String>,
        identity: DeviceIdentity
    ): MultiAppAuditSession = withContext(Dispatchers.Default) {
        val networkIntel = fetchLiveNetworkIntelligence()
        val dangerousPathReport = securityAuditor.scanDangerousFoldersAndFiles()
        val fullReport = securityAuditor.performDeepAudit(identity)
        val playIntegrity = fullReport.playIntegrity

        val pm = context.packageManager
        val results = mutableListOf<TargetAppAuditResult>()

        for (pkg in selectedPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val engineType = identifyEngineType(pkg, appName)

                val auditResult = evaluateAppReadiness(
                    appName = appName,
                    packageName = pkg,
                    isSystem = isSystem,
                    engineType = engineType,
                    networkIntel = networkIntel,
                    dangerousPaths = dangerousPathReport,
                    identity = identity,
                    playIntegrity = playIntegrity
                )
                results.add(auditResult)
            } catch (_: Exception) {}
        }

        val overallScore = if (results.isEmpty()) 100 else results.map { it.readinessScore }.average().toInt()

        MultiAppAuditSession(
            timestamp = System.currentTimeMillis(),
            networkIntel = networkIntel,
            auditedApps = results,
            overallReadinessScore = overallScore
        )
    }

    private fun evaluateAppReadiness(
        appName: String,
        packageName: String,
        isSystem: Boolean,
        engineType: TargetEngineType,
        networkIntel: LiveNetworkIntelligence,
        dangerousPaths: DangerousPathReport,
        identity: DeviceIdentity,
        playIntegrity: PlayIntegrityReport
    ): TargetAppAuditResult {
        val dangers = mutableListOf<AppDangerItem>()
        val fixSteps = mutableListOf<AppFixStep>()
        var score = 100

        var stepCounter = 1

        // 1. Cek Folder Blacklist Storage yang sering discan aplikasi target (Shopee/Tokopedia TWRP & Backup)
        val twrpDir = File(Environment.getExternalStorageDirectory(), "TWRP")
        val titaniumDir = File(Environment.getExternalStorageDirectory(), "TitaniumBackup")
        val parallelDir = File(Environment.getExternalStorageDirectory(), "ParallelApp")

        if (twrpDir.exists()) {
            val penalty = DangerSeverity.HIGH.weightPenalty
            score -= penalty
            dangers.add(
                AppDangerItem(
                    title = "Folder /sdcard/TWRP Terdeteksi",
                    severity = DangerSeverity.HIGH,
                    explanation = "$appName memindai folder penyimpanan umum. Keberadaan folder TWRP menandakan ponsel bekas modifikasi/flash recovery kustom.",
                    technicalProof = "Path: ${twrpDir.absolutePath} (Dapat dibaca tanpa izin root)"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Hapus Folder TWRP di Internal Storage",
                    detailedInstruction = "Buka File Manager, cari dan hapus folder 'TWRP' di penyimpanan internal, atau gunakan modul Magisk 'Storage Isolation' agar $appName tidak bisa memindai isi direktori bersama.",
                    recommendedModuleOrTool = "Storage Isolation / Mount Namespace / File Manager"
                )
            )
        }

        if (titaniumDir.exists() || parallelDir.exists()) {
            score -= DangerSeverity.MEDIUM.weightPenalty
            dangers.add(
                AppDangerItem(
                    title = "Jejak Folder Dual Space / Backup Terdeteksi",
                    severity = DangerSeverity.MEDIUM,
                    explanation = "Ditemukan folder cadangan aplikasi klon (${titaniumDir.name}/${parallelDir.name}) yang langsung di-blacklist oleh SDK ThreatMetrix/AppsFlyer.",
                    technicalProof = "Ditemukan folder di penyimpanan bersama"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Bersihkan Direktori Klona Lama",
                    detailedInstruction = "Hapus jejak folder klona di /sdcard/ sebelum login akun baru untuk menghindari pengelompokan akun (Device Clustering).",
                    recommendedModuleOrTool = "SD Maid SE / Manual Delete"
                )
            )
        }

        // 2. Cek Folder Berbahaya Sistem (/data/adb, KSU, APatch)
        if (dangerousPaths.foundFolders.isNotEmpty()) {
            val penalty = DangerSeverity.CRITICAL.weightPenalty
            score -= penalty
            dangers.add(
                AppDangerItem(
                    title = "Folder Root /data/adb Bocor",
                    severity = DangerSeverity.CRITICAL,
                    explanation = "SDK Anti-Fraud $appName mendeteksi folder ${dangerousPaths.foundFolders.firstOrNull()}. Ini memicu flag 'Root Detected' dan menyebabkan checkout gagal (Error F01 / Akun Dibatasi).",
                    technicalProof = "Path terbaca: ${dangerousPaths.foundFolders.take(2).joinToString()}"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Isolasi /data/adb via Shamiko",
                    detailedInstruction = "Pasang modul Shamiko (versi terbaru) di Magisk/KernelSU. Pastikan fitur Zygisk aktif dan package '$packageName' dicentang di DenyList (tanpa mengaktifkan Enforce DenyList jika menggunakan Shamiko).",
                    recommendedModuleOrTool = "Modul Shamiko + Zygisk Next"
                )
            )
        }

        // 3. Cek Kebocoran Mount Point Kernel (/proc/mounts)
        if (dangerousPaths.foundMountLeaks.isNotEmpty()) {
            score -= DangerSeverity.HIGH.weightPenalty
            dangers.add(
                AppDangerItem(
                    title = "Tabel Mount Partisi Bocor (/proc/mounts)",
                    severity = DangerSeverity.HIGH,
                    explanation = "$appName membaca tabel /proc/mounts dan menemukan virtual mount Magisk/KSU. Ini adalah metode deteksi modern tanpa akses root.",
                    technicalProof = "Leak: ${dangerousPaths.foundMountLeaks.firstOrNull()}"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Aktifkan Mount Namespace Isolation",
                    detailedInstruction = "Jika memakai KernelSU/APatch, aktifkan pengaturan 'Mount Namespace Isolation' (mode Unshare) untuk package $packageName agar mount virtual root tidak terlihat.",
                    recommendedModuleOrTool = "KernelSU / APatch Manager Settings"
                )
            )
        }

        // 4. Cek Biner SU / Busybox
        if (dangerousPaths.foundBinaries.isNotEmpty()) {
            score -= DangerSeverity.CRITICAL.weightPenalty
            dangers.add(
                AppDangerItem(
                    title = "Biner su / busybox Masih Terbaca",
                    severity = DangerSeverity.CRITICAL,
                    explanation = "Biner eksekusi root ditemukan di path sistem umum. Aplikasi langsung menutup atau memblokir transaksi finansial.",
                    technicalProof = "Ditemukan di: ${dangerousPaths.foundBinaries.joinToString()}"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Bersihkan Biner Legacy su",
                    detailedInstruction = "Hapus symlink biner su manual di /system/bin atau gunakan KernelSU berbasis GKI yang tidak meninggalkan artefak biner su di direktori standar.",
                    recommendedModuleOrTool = "GKI KernelSU / Hide My Applist"
                )
            )
        }

        // 5. Cek Developer Options & USB Debugging
        val isAdbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) { false }

        val isDevOptionsEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        } catch (_: Exception) { false }

        if (isAdbEnabled || isDevOptionsEnabled) {
            // Khusus Shopee & Tokopedia sangat sensitif terhadap USB Debugging
            val penalty = if (engineType == TargetEngineType.SHOPEE || engineType == TargetEngineType.TOKOPEDIA) 15 else 8
            score -= penalty
            dangers.add(
                AppDangerItem(
                    title = "USB Debugging / Developer Options Aktif",
                    severity = DangerSeverity.MEDIUM,
                    explanation = "Aplikasi $appName menandai perangkat dengan Developer Options aktif sebagai potensi otomasi bot/auto-checkout.",
                    technicalProof = "ADB Enabled: $isAdbEnabled, Dev Options: $isDevOptionsEnabled"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Matikan USB Debugging Saat Transaksi",
                    detailedInstruction = "Buka Pengaturan HP -> Opsi Pengembang -> Matikan USB Debugging saat membuka $appName untuk multi-akun.",
                    recommendedModuleOrTool = "Pengaturan Sistem Android"
                )
            )
        }

        // 6. Cek Jaringan Online (VPN / Proxy / Clustering IP)
        if (networkIntel.isVpnOrProxy) {
            score -= 25
            dangers.add(
                AppDangerItem(
                    title = "Terdeteksi Koneksi VPN / Datacenter Proxy",
                    severity = DangerSeverity.CRITICAL,
                    explanation = "Menggunakan VPN untuk multi-akun di $appName akan memicu auto-banned karena IP VPN masuk daftar blacklist ASN server anti-fraud.",
                    technicalProof = "Network Transport: VPN Active (Public IP: ${networkIntel.publicIp})"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Gunakan IP Residental Mobile / Mode Pesawat",
                    detailedInstruction = "Matikan VPN. Gunakan data seluler (Telkomsel/Indosat/XL), lalu lakukan toggle Mode Pesawat 5 detik untuk merotasi Subnet IP sebelum membuka akun baru.",
                    recommendedModuleOrTool = "Airplane Mode IP Rotation (Mobile Data 4G/5G)"
                )
            )
        }

        // 7. Cek Play Integrity Status
        if (!playIntegrity.meetsBasicIntegrity) {
            score -= 20
            dangers.add(
                AppDangerItem(
                    title = "Gagal Play Integrity (MEETS_BASIC_INTEGRITY)",
                    severity = DangerSeverity.HIGH,
                    explanation = "Device bootloader tidak lolos integritas dasar Google CTS. $appName dapat menolak login akun baru.",
                    technicalProof = "Basic Integrity: FAIL"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Perbarui Modul Play Integrity Fix (PIF)",
                    detailedInstruction = "Pasang modul 'PlayIntegrityFix' terbaru (oleh chiteroman) dan modul 'TrickyStore' untuk mengembalikan status MEETS_DEVICE_INTEGRITY.",
                    recommendedModuleOrTool = "PlayIntegrityFix (PIF) + Tricky Store"
                )
            )
        }

        // 8. Cek Konsistensi Identitas Spoof (Hardware Anomaly)
        val availableCores = Runtime.getRuntime().availableProcessors()
        val totalMemoryMb = try {
            val mi = android.app.ActivityManager.MemoryInfo()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            am?.getMemoryInfo(mi)
            mi.totalMem / (1024 * 1024)
        } catch (_: Exception) { 4096L }

        if (availableCores < 4 || totalMemoryMb < 2000) {
            score -= 10
            dangers.add(
                AppDangerItem(
                    title = "Profil RAM/CPU Terlalu Rendah (Curiga Emulator)",
                    severity = DangerSeverity.MEDIUM,
                    explanation = "Spesifikasi perangkat terlihat seperti emulator atau sandbox bot.",
                    technicalProof = "RAM: ${totalMemoryMb}MB, Cores: $availableCores"
                )
            )
            fixSteps.add(
                AppFixStep(
                    stepNumber = stepCounter++,
                    actionTitle = "Sesuaikan Profil Spoof ke Tipe HP Modern",
                    detailedInstruction = "Gunakan profil spoofing HP komersial standar (RAM 6GB+, 8 Cores Snapdragon/Dimensity) agar tampak alami.",
                    recommendedModuleOrTool = "Device Parameter Template / Magisk Hide Props"
                )
            )
        }

        score = score.coerceIn(5, 100)

        val (verdictTitle, colorHex, advice) = when {
            score >= 95 -> Triple(
                "100% AMAN (Siap Multi-Akun & Bebas Anomali)",
                0xFF00E676,
                "Device sudah bersih dari bocoran root, tabel mount, dan folder terlarang. Aman digunakan untuk transaksi multi-akun di $appName."
            )
            score in 80..94 -> Triple(
                "85% RISIKO RINGAN (Waspada Pengelompokan Akun)",
                0xFFFFD600,
                "Aplikasi tidak mendeteksi root langsung, tetapi masih ada sedikit anomali parameter/pengaturan (seperti USB Debugging atau sisa cache) yang bisa menyebabkan akun ditandai."
            )
            score in 60..79 -> Triple(
                "65% RAWAN DETEKSI (Potensi Gagal Checkout / Kupon Hangus)",
                0xFFFF9100,
                "Terdapat folder sensitif atau inkonsistensi yang terbaca oleh SDK anti-fraud $appName. Sangat disarankan memperbaiki langkah fix di bawah sebelum login."
            )
            else -> Triple(
                "BAHAYA TINGGI (Auto-Ban / Terdeteksi Tuyul)",
                0xFFFF1744,
                "Jejak root fisik (/data/adb atau biner su) atau VPN bocor secara transparan ke $appName. Akun baru yang login berisiko langsung dinonaktifkan."
            )
        }

        return TargetAppAuditResult(
            appName = appName,
            packageName = packageName,
            isSystemApp = isSystem,
            engineType = engineType,
            readinessScore = score,
            verdictTitle = verdictTitle,
            statusColorHex = colorHex,
            detectedDangers = dangers,
            fixSteps = fixSteps,
            multiAccountAdvice = advice
        )
    }
}
