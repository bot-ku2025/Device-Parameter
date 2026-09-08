package com.example.data.model

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
    val isRiskTarget: Boolean,
    val categoryLabel: String,
    val targetEngineType: TargetEngineType
)

enum class TargetEngineType(
    val displayName: String,
    val fraudSdkProfile: String,
    val riskContext: String
) {
    SHOPEE(
        "Shopee Security Shield",
        "SudoHide, In-house Bot Engine, Storage TWRP Scan, Canvas Webview",
        "Checkout, Voucher Promo & Anti-Tuyul M02/F01"
    ),
    TOKOPEDIA(
        "Tokopedia / GoPay ThreatMetrix",
        "ThreatMetrix (LexisNexis), AppsFlyer, Mount Namespace Scan, Su Binary Probe",
        "Proteksi Transaksi, Login Multi-Akun & Kupon Diskon"
    ),
    BYTEDANCE_VIDEO(
        "ByteDance Security Guardian",
        "ByteDance In-house Sec, GAID Tracking, Wi-Fi BSSID Clustering, Hardware Device ID",
        "Nonton Drama / Koin Reward / Anti Multi-Device Ban"
    ),
    FINANCIAL_BANKING(
        "Bank & E-Wallet Shield",
        "Hardware Play Integrity, RootBeer, Frida/Xposed Hook, Accessibility Scanner",
        "Proteksi Saldo Finansial & Pembayaran E-Wallet"
    ),
    RIDE_HAILING(
        "Ojol / Logistik Location Shield",
        "Mock Location Detection, Developer Options, Fused Location Provider Bypass",
        "Deteksi GPS Palsu / Fake GPS Tuyul"
    ),
    GENERIC_COMMERCE(
        "E-Commerce & Marketplaces",
        "AppsFlyer, Device ID Cloning Check, Root & Busybox Basic Scans",
        "Multi-Akun Belanja & Pendaftaran Akun Baru"
    ),
    ENTERTAINMENT_GAME(
        "Game & Social Media Anti-Cheat",
        "Hardware Device Ban, Multiple Accounts Limiter, Emulation Sandbox Check",
        "Multi-Akun Nonton/Game & Anti-Banned Perangkat"
    ),
    SYSTEM_SERVICE(
        "Layanan Sistem Android",
        "Android OS Internal Component",
        "Stabilitas & Kompatibilitas Framework"
    )
}

data class LiveNetworkIntelligence(
    val isOnline: Boolean = false,
    val publicIp: String = "Offline / Unknown",
    val isp: String = "Unknown ISP",
    val country: String = "ID",
    val isVpnOrProxy: Boolean = false,
    val subnetClusterRisk: String = "Rendah",
    val dynamicRuleVersion: String = "v2026.09-LIVE-SYNCED"
)

data class TargetAppAuditResult(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val engineType: TargetEngineType,
    val readinessScore: Int, // 0 - 100
    val verdictTitle: String,
    val statusColorHex: Long,
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
    CRITICAL("KRITIKAL", 25),
    HIGH("TINGGI", 15),
    MEDIUM("SEDANG", 8),
    INFO("PERINGATAN", 4)
}

data class AppFixStep(
    val stepNumber: Int,
    val actionTitle: String,
    val detailedInstruction: String,
    val ksuFix: String,
    val magiskFix: String,
    val apatchFix: String,
    val generalAction: String
)

data class MultiAppAuditSession(
    val timestamp: Long = System.currentTimeMillis(),
    val networkIntel: LiveNetworkIntelligence = LiveNetworkIntelligence(),
    val auditedApps: List<TargetAppAuditResult> = emptyList(),
    val overallReadinessScore: Int = 100
)
