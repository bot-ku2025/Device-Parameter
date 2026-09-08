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
    SHORT_DRAMA_REWARD(
        "Short Drama & Video Reward Engine",
        "ByteDance/Tencent Video Sec, GAID Tracking, Subnet Clustering, Watch-Time Bot Guard",
        "Nonton Drama Koin, Event Tugas Harian & Anti-Shadowban"
    ),
    REWARD_GAME(
        "Game Koin & Reward Anti-Bot Engine",
        "Unity Fraud Guard, In-House Bot Detection, Auto-Clicker Sensor, Device ID Clustering",
        "Klaim Saldo Koin, Nonton Iklan Reward & Anti Multi-Akun Ban"
    ),
    RETAIL_LOYALTY(
        "Minimarket & Retail Loyalty Shield",
        "Alfagift/Indomaret Member Guard, Mock GPS Detector, Multi-Akun Voucher Kasir",
        "Klaim Voucher Belanja Gratis, Poin Member & Scan Barcode Kasir"
    ),
    FINANCIAL_BANKING(
        "Bank & E-Wallet Security Shield",
        "Hardware Play Integrity, RootBeer, Frida/Xposed Hook, Accessibility Guard",
        "Proteksi Saldo Finansial & Pembayaran E-Wallet"
    ),
    GOOGLE_ECOSYSTEM(
        "Google Ecosystem & AI Integrity",
        "Google DroidGuard, GSF ID Verification, Play Protect SafetyNet, Webview Sandbox",
        "Otentikasi Akun Google, AI Assistant & Layanan Cloud Sync"
    ),
    HARDWARE_UTILITY(
        "Hardware Benchmark & Sensor Diagnostics",
        "Direct Kernel /sys/class/ Reading, Thermal Sensors, Battery Raw Probing",
        "Pengukuran Tegangan, Suhu Baterai & Diagnostik SoC"
    ),
    RIDE_HAILING(
        "Ojol / Logistik Location Shield",
        "Mock Location Detection, Developer Options, Fused Location Provider Bypass",
        "Deteksi GPS Palsu / Fake GPS Tuyul & Order Gacor"
    ),
    SOCIAL_MESSAGING(
        "Media Sosial & Chat Multi-Akun",
        "Device Fingerprint Ban, IP Subnet Rate Limiter, Multi-Account Session Clustering",
        "Login Banyak Akun Chat, Anti-Spam & Broadcast Otomatis"
    ),
    GENERIC_COMMERCE(
        "E-Commerce & Marketplaces",
        "AppsFlyer, Device ID Cloning Check, Root & Busybox Basic Scans",
        "Multi-Akun Belanja & Pendaftaran Akun Baru"
    ),
    COMPETITIVE_GAME(
        "Game Online & Anti-Cheat Shield",
        "Hardware Device Ban, Memory Tamper Guard, Emulation Sandbox Check",
        "Multi-Akun Game, Anti-Banned Perangkat & Keamanan Akun"
    ),
    GENERAL_APP(
        "Aplikasi Produktivitas & Standar",
        "Android Standard Sandbox, Storage Access Framework",
        "Penggunaan Normal & Stabilitas Aplikasi"
    ),
    SYSTEM_SERVICE(
        "Layanan Sistem Android OS",
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
