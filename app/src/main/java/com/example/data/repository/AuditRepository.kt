package com.example.data.repository

import com.example.data.dao.AuditLogDao
import com.example.data.model.SecurityAuditLog
import kotlinx.coroutines.flow.Flow

class AuditRepository(private val auditLogDao: AuditLogDao) {
    val allLogs: Flow<List<SecurityAuditLog>> = auditLogDao.getAllLogs()

    suspend fun logEvent(
        action: String,
        details: String,
        category: String,
        severity: String = "INFO"
    ): Long {
        val log = SecurityAuditLog(
            action = action,
            details = details,
            category = category,
            severity = severity
        )
        return auditLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        auditLogDao.clearAllLogs()
    }
}
