package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_records")
data class AuditRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val logId: String,
    val timestamp: String,
    val tenantId: String,
    val actorId: String,
    val workflowId: String,
    val actionType: String,
    val toolName: String,
    val policyVerdict: String,
    val inputHash: String,
    val outputHash: String? = null,
    val previousLogHash: String,
    val currentHash: String,
    val details: String,
    val isTampered: Boolean = false
)
