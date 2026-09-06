package com.example.data.db

import kotlinx.coroutines.flow.Flow

class AuditRepository(private val dao: AuditLogDao) {
    val allLogs: Flow<List<AuditLogEntity>> = dao.getAllLogs()

    suspend fun getLatest(): AuditLogEntity? = dao.getLatestLog()

    suspend fun saveScan(log: AuditLogEntity): Long = dao.insertLog(log)

    suspend fun deleteScan(id: Long) = dao.deleteById(id)

    suspend fun clearHistory() = dao.clearAll()
}
