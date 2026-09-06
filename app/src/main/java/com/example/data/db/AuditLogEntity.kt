package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val stealthLevel: String,
    val passedCount: Int,
    val warnCount: Int,
    val failCount: Int,
    val brandModel: String,
    val rootDetected: Boolean,
    val playIntegrityStatus: String,
    val summary: String
)
