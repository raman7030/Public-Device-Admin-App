package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_audit_logs")
data class SecurityAuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val details: String,
    val category: String, // SCREEN_POLICY, PASSWORD_POLICY, ENROLLMENT, DEVICE_LOCK, COMPLIANCE
    val severity: String  // INFO, SUCCESS, WARNING, DANGER
)
