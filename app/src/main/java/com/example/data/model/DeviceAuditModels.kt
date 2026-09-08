package com.example.data.model

enum class CheckStatus {
    PENDING,
    PASS,
    WARN,
    FAIL
}

enum class SecurityCategory(val title: String) {
    ROOT_ACCESS("Akses Root"),
    KERNEL_SELINUX("Kernel & SELinux"),
    BOOTLOADER("Bootloader & Verified Boot"),
    PLAY_INTEGRITY("Google Play Integrity"),
    DEVICE_PROPS("System Properties Consistency"),
    IDENTIFIER_SPOOF("Device Identifiers Spoof Check"),
    EXPLOIT_HOOKS("Hook & Tamper Detection")
}

data class ParameterDiagnostic(
    val key: String,
    val title: String,
    val value: String,
    val status: CheckStatus,
    val trackingAnalysis: String,
    val fixGuide: String
)

data class SecurityCheckItem(
    val id: String,
    val title: String,
    val category: SecurityCategory,
    val status: CheckStatus,
    val detail: String,
    val technicalLog: String = "",
    val trackingAnalysis: String = "",
    val fixGuide: String = ""
)

data class DeviceHardwareStats(
    val cpuUsagePercent: Float = 0f,
    val cpuCores: Int = 8,
    val cpuArch: String = "aarch64",
    val ramUsedMb: Long = 0L,
    val ramTotalMb: Long = 0L,
    val ramAvailMb: Long = 0L,
    val ramUsagePercent: Float = 0f,
    val isLowMemory: Boolean = false,
    val networkType: String = "Wi-Fi",
    val ipAddress: String = "127.0.0.1",
    val wifiSsid: String = "<Not Connected>",
    val linkSpeedMbps: Int = 0,
    val hasVpn: Boolean = false
)

data class DeviceIdentity(
    val brand: String = "",
    val model: String = "",
    val androidVersion: String = "",
    val sdkInt: Int = 0,
    val techModel: String = "",
    val codename: String = "",
    val boardPlatform: String = "",
    val androidId: String = "",
    val imei1: String = "",
    val imei2: String = "",
    val serial: String = "",
    val fingerprint: String = "",
    val gsfId: String = "",
    val wifiMac: String = "",
    val wifiSsid: String = "",
    val wifiBssid: String = "",
    val bluetoothMac: String = "",
    val advertisingId: String = "",
    val appSetId: String = "",
    val widevineDrmId: String = "",
    val userAgent: String = "",
    val installerPackage: String = "",
    val hiddenKeyboardPackages: String = "",
    val virtualDefaultIme: String = "",
    val nearbyBtName: String = "",
    val nearbyBtAddress: String = "",
    val isSpoofed: Boolean = false,
    val spoofDetectionDetail: String = "",
    val baseHardwareInfo: String = "",
    val parameterStatuses: Map<String, CheckStatus> = emptyMap(),
    val diagnostics: Map<String, ParameterDiagnostic> = emptyMap()
)

data class PlayIntegrityReport(
    val meetsBasicIntegrity: Boolean = true,
    val meetsDeviceIntegrity: Boolean = false,
    val meetsStrongIntegrity: Boolean = false,
    val isPlayProtectCertified: Boolean = false,
    val evaluationType: String = "HARDWARE_BACKED_EVAL_FAILED",
    val summary: String = "Evaluasi Play Integrity API"
)

data class SpoofAuditScore(
    val overallScore: Int = 100, // 0 - 100
    val stealthLevel: String = "Stealth / Safe",
    val totalChecks: Int = 0,
    val passedCount: Int = 0,
    val warnCount: Int = 0,
    val failCount: Int = 0,
    val riskSummary: String = "",
    val recommendations: List<String> = emptyList()
)

data class SpoofLeakItem(
    val layer: String,
    val parameter: String,
    val spoofedValue: String,
    val leakedRealValue: String,
    val riskImpact: String,
    val fixSolution: String
)

data class SpoofDepthAnalysis(
    val depthScorePercent: Int = 0,
    val depthTier: String = "Surface Only",
    val surfaceScore: Int = 100,
    val vendorPropsScore: Int = 100,
    val hardwareLeakScore: Int = 100,
    val integrityScore: Int = 100,
    val detectedLeaks: List<SpoofLeakItem> = emptyList(),
    val multiAccountSafetyVerdict: String = "",
    val antiFraudDetectionLikelihood: String = "Tinggi (Mudah Dideteksi SDK Risk)"
)

data class DangerousPathReport(
    val foundFolders: List<String> = emptyList(),
    val foundBinaries: List<String> = emptyList(),
    val foundMountLeaks: List<String> = emptyList()
) {
    val hasDanger: Boolean get() = foundFolders.isNotEmpty() || foundBinaries.isNotEmpty() || foundMountLeaks.isNotEmpty()
}

data class FullAuditReport(
    val timestamp: Long = System.currentTimeMillis(),
    val identity: DeviceIdentity = DeviceIdentity(),
    val hardwareStats: DeviceHardwareStats = DeviceHardwareStats(),
    val securityChecks: List<SecurityCheckItem> = emptyList(),
    val playIntegrity: PlayIntegrityReport = PlayIntegrityReport(),
    val score: SpoofAuditScore = SpoofAuditScore(),
    val spoofDepth: SpoofDepthAnalysis = SpoofDepthAnalysis(),
    val dangerousPathReport: DangerousPathReport = DangerousPathReport()
)
