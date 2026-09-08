package com.example.data.model

import android.graphics.drawable.Drawable

enum class AppTypeFilter {
    ALL,
    USER,
    SYSTEM
}

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val isRiskTarget: Boolean, // e.g. E-Commerce, E-Wallet, Banking, Social Media
    val categoryLabel: String,
    val targetEngineType: TargetEngineType
)

enum class TargetEngineType(val displayName: String, val fraudSdkProfile: String) {
    SHOPEE("Shopee Fraud Shield", "SudoHide Detection, In-house Bot Engine, Storage TWRP Scan, Canvas Fingerprint"),
    TOKOPEDIA("Tokopedia / GoPay", "ThreatMetrix (LexisNexis), AppsFlyer, Mount Namespace Scan, Su Binary Probe"),
    TIKTOK("ByteDance Security Guardian", "Sensor Telemetry Gyro/Accel, Wi-Fi BSSID Clustering, Hardware CTS Fingerprint"),
    FINANCIAL_BANKING("Bank & E-Wallet Shield", "Hardware-Backed Play Integrity, RootBeer, Frida/Xposed Hook, Accessibility Probe"),
    RIDE_HAILING("Gojek / Grab / Maxim", "Mock Location Detection, Developer Options, Fused Location Provider Bypass"),
    GENERIC_COMMERCE("Standard Anti-Fraud Engine", "AppsFlyer, Device ID Cloning Check, Root & Busybox Basic Scans")
}

data class LiveNetworkIntelligence(
    val isOnline: Boolean = false,
    val publicIp: String = "Offline / Unknown",
    val isp: String = "Unknown ISP",
    val country: String = "ID",
    val isVpnOrProxy: Boolean = false,
    val subnetClusterRisk: String = "Rendah",
    val dynamicRuleVersion: String = "v2026.09-LATEST"
)

data class TargetAppAuditResult(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val engineType: TargetEngineType,
    val readinessScore: Int, // 0 - 100
    val verdictTitle: String, // e.g. "100% AMAN (Siap Multi-Akun & Checkout)"
    val statusColorHex: Long, // Green, Amber, Red
    val detectedDangers: List<AppDangerItem>,
    val fixSteps: List<AppFixStep>,
    val multiAccountAdvice: String
)

data class AppDangerItem(
    val title: String,
    val severity: DangerSeverity,
    val explanation: String,
    val technicalProof: String
)

enum class DangerSeverity(val label: String, val weightPenalty: Int) {
    CRITICAL("KRITIKAL (Auto-Ban)", 35),
    HIGH("TINGGI (Cekal Akun)", 20),
    MEDIUM("SEDANG (Kupon Hilang)", 10),
    INFO("PERINGATAN (Clustering)", 5)
}

data class AppFixStep(
    val stepNumber: Int,
    val actionTitle: String,
    val detailedInstruction: String,
    val recommendedModuleOrTool: String
)

data class MultiAppAuditSession(
    val timestamp: Long = System.currentTimeMillis(),
    val networkIntel: LiveNetworkIntelligence = LiveNetworkIntelligence(),
    val auditedApps: List<TargetAppAuditResult> = emptyList(),
    val overallReadinessScore: Int = 100
)
