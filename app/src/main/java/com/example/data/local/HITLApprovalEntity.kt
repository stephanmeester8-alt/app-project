package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ApprovalStatus
import com.example.data.model.RiskLevel
import com.example.data.model.ToolActionType

@Entity(tableName = "hitl_approvals")
data class HITLApprovalEntity(
    @PrimaryKey
    val approvalId: String,
    val tenantId: String,
    val workflowId: String,
    val actionType: ToolActionType,
    val toolName: String,
    val riskLevel: RiskLevel,
    val target: String,
    val description: String,
    val payloadJson: String,
    val hmacSignature: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000),
    val status: ApprovalStatus = ApprovalStatus.PENDING,
    val resolvedBy: String? = null,
    val resolvedAt: Long? = null
)
