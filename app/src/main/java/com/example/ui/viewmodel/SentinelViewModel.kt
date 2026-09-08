package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AuditLogEntity
import com.example.data.db.AuditRepository
import com.example.data.db.SentinelDatabase
import com.example.data.model.DeviceHardwareStats
import com.example.data.model.DeviceIdentity
import com.example.data.model.FullAuditReport
import com.example.data.model.InstalledAppInfo
import com.example.data.model.MultiAppAuditSession
import com.example.util.HardwareMonitor
import com.example.util.NotificationHelper
import com.example.util.SecurityAuditor
import com.example.util.TargetAppAuditor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AuditScanState {
    object Idle : AuditScanState()
    data class Scanning(val progress: Float, val currentStage: String) : AuditScanState()
    data class Completed(val report: FullAuditReport) : AuditScanState()
}

sealed class TargetAuditState {
    object Idle : TargetAuditState()
    data class Running(val progress: Float, val stage: String) : TargetAuditState()
    data class Done(val session: MultiAppAuditSession) : TargetAuditState()
}

class SentinelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuditRepository
    private val hardwareMonitor = HardwareMonitor(application)
    private val securityAuditor = SecurityAuditor(application)
    private val targetAppAuditor = TargetAppAuditor(application)

    private val _hardwareStats = MutableStateFlow(DeviceHardwareStats())
    val hardwareStats: StateFlow<DeviceHardwareStats> = _hardwareStats.asStateFlow()

    private val _identity = MutableStateFlow(DeviceIdentity())
    val identity: StateFlow<DeviceIdentity> = _identity.asStateFlow()

    private val _auditState = MutableStateFlow<AuditScanState>(AuditScanState.Idle)
    val auditState: StateFlow<AuditScanState> = _auditState.asStateFlow()

    private val _latestReport = MutableStateFlow<FullAuditReport?>(null)
    val latestReport: StateFlow<FullAuditReport?> = _latestReport.asStateFlow()

    private val _anomalyAlertsEnabled = MutableStateFlow(true)
    val anomalyAlertsEnabled: StateFlow<Boolean> = _anomalyAlertsEnabled.asStateFlow()

    // Target Engine States
    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _selectedPackageNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackageNames: StateFlow<Set<String>> = _selectedPackageNames.asStateFlow()

    private val _targetAuditState = MutableStateFlow<TargetAuditState>(TargetAuditState.Idle)
    val targetAuditState: StateFlow<TargetAuditState> = _targetAuditState.asStateFlow()

    private val _latestTargetSession = MutableStateFlow<MultiAppAuditSession?>(null)
    val latestTargetSession: StateFlow<MultiAppAuditSession?> = _latestTargetSession.asStateFlow()

    private var lastAnomalyNotificationTime = 0L
    private var monitorJob: Job? = null

    val historyLogs: StateFlow<List<AuditLogEntity>>

    init {
        val db = SentinelDatabase.getInstance(application)
        repository = AuditRepository(db.auditLogDao())
        NotificationHelper.createNotificationChannel(application)

        historyLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load initial device parameters
        _identity.value = securityAuditor.collectDeviceIdentity()

        // Load installed apps
        loadInstalledApps()

        // Start realtime hardware monitor loop
        startHardwareMonitor()
    }

    private fun startHardwareMonitor() {
        monitorJob?.cancel()
        monitorJob = viewModelScope.launch {
            while (isActive) {
                val stats = hardwareMonitor.getRealtimeStats()
                _hardwareStats.value = stats

                // Check for performance anomaly
                checkPerformanceAnomaly(stats)

                delay(1200)
            }
        }
    }

    private fun checkPerformanceAnomaly(stats: DeviceHardwareStats) {
        if (!_anomalyAlertsEnabled.value) return
        val now = System.currentTimeMillis()
        if (now - lastAnomalyNotificationTime < 45_000) return // Throttle 45s

        if (stats.cpuUsagePercent > 88f) {
            lastAnomalyNotificationTime = now
            NotificationHelper.sendAnomalyNotification(
                getApplication(),
                1001,
                "⚠️ Anomali CPU Terdeteksi!",
                "Beban CPU melonjak ke ${stats.cpuUsagePercent.toInt()}%. Periksa background process atau thread yang hang."
            )
        } else if (stats.ramUsagePercent > 90f || stats.isLowMemory) {
            lastAnomalyNotificationTime = now
            NotificationHelper.sendAnomalyNotification(
                getApplication(),
                1002,
                "⚠️ Peringatan Tekanan RAM!",
                "Penggunaan memori sistem mencapai ${stats.ramUsagePercent.toInt()}%. Risiko Low Memory Killer (LMK) aktif."
            )
        }
    }

    fun startDeepAudit() {
        if (_auditState.value is AuditScanState.Scanning) return

        viewModelScope.launch {
            val stages = listOf(
                Pair(0.15f, "Memindai akses root, biner SU & manajer Magisk/KernelSU..."),
                Pair(0.35f, "Memeriksa status SELinux Enforcing, kernel version & bootloader..."),
                Pair(0.60f, "Mengevaluasi tanda tangan Google Play Integrity & Play Protect..."),
                Pair(0.80f, "Menganalisis konsistensi parameter spoofing & Build Props..."),
                Pair(0.95f, "Mengompilasi skor stealth perangkat & deteksi anomali...")
            )

            for ((progress, stageName) in stages) {
                _auditState.value = AuditScanState.Scanning(progress, stageName)
                delay(600)
            }

            // Execute full deep audit
            val report = securityAuditor.performDeepAudit(_identity.value)
            _identity.value = report.identity
            _latestReport.value = report
            _auditState.value = AuditScanState.Completed(report)

            // Save to Room DB history
            repository.saveScan(
                AuditLogEntity(
                    score = report.score.overallScore,
                    stealthLevel = report.score.stealthLevel,
                    passedCount = report.score.passedCount,
                    warnCount = report.score.warnCount,
                    failCount = report.score.failCount,
                    brandModel = "${report.identity.brand} ${report.identity.model}",
                    rootDetected = report.securityChecks.any { it.category == com.example.data.model.SecurityCategory.ROOT_ACCESS && it.status == com.example.data.model.CheckStatus.FAIL },
                    playIntegrityStatus = if (report.playIntegrity.meetsDeviceIntegrity) "PASS (Device Integrity)" else "FAIL (Uncertified/Broken)",
                    summary = report.score.riskSummary
                )
            )
        }
    }

    fun toggleAnomalyAlerts() {
        _anomalyAlertsEnabled.value = !_anomalyAlertsEnabled.value
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = targetAppAuditor.getInstalledApps()
            _installedApps.value = apps
            // Auto-select risk targets initially if empty
            if (_selectedPackageNames.value.isEmpty()) {
                val defaultSelected = apps.filter { it.isRiskTarget }.take(3).map { it.packageName }.toSet()
                _selectedPackageNames.value = defaultSelected
            }
        }
    }

    fun toggleAppSelection(packageName: String) {
        val current = _selectedPackageNames.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedPackageNames.value = current
    }

    fun selectAllFiltered(packageNames: List<String>) {
        val current = _selectedPackageNames.value.toMutableSet()
        current.addAll(packageNames)
        _selectedPackageNames.value = current
    }

    fun clearSelectedApps() {
        _selectedPackageNames.value = emptySet()
    }

    fun runTargetAppAudit() {
        val selected = _selectedPackageNames.value.toList()
        if (selected.isEmpty() || _targetAuditState.value is TargetAuditState.Running) return

        viewModelScope.launch {
            _targetAuditState.value = TargetAuditState.Running(0.15f, "Menghubungkan ke Cloud Network Intelligence...")
            delay(500)
            _targetAuditState.value = TargetAuditState.Running(0.40f, "Memeriksa IP Publik, DNS & Risiko Clustering Subnet...")
            delay(600)
            _targetAuditState.value = TargetAuditState.Running(0.70f, "Mencocokkan signature anti-fraud ${selected.size} aplikasi target...")
            delay(700)
            _targetAuditState.value = TargetAuditState.Running(0.90f, "Menguji kebocoran direktori /sdcard/TWRP & /proc/mounts...")
            delay(500)

            val session = targetAppAuditor.auditSelectedApps(selected, _identity.value)
            _latestTargetSession.value = session
            _targetAuditState.value = TargetAuditState.Done(session)
        }
    }
}
