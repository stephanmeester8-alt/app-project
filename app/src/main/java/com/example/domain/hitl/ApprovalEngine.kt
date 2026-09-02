package com.example.domain.hitl

import com.example.data.local.AppDatabase
import com.example.data.local.HITLApprovalEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.PolicyVerdict
import com.example.data.model.RiskLevel
import com.example.data.model.TenantContext
import com.example.data.model.TenantRole
import com.example.data.model.ToolActionType
import com.example.domain.audit.AuditLogger
import com.example.domain.crypto.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class ApprovalResolutionEvent {
    data class Resolved(val approval: HITLApprovalEntity, val decision: ApprovalStatus) : ApprovalResolutionEvent()
}

class ApprovalEngine(
    private val database: AppDatabase,
    private val auditLogger: AuditLogger
) {
    private val _events = MutableSharedFlow<ApprovalResolutionEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ApprovalResolutionEvent> = _events.asSharedFlow()

    suspend fun createApprovalRequest(
        context: TenantContext,
        workflowId: String,
        toolName: String,
        actionType: ToolActionType,
        riskLevel: RiskLevel,
        target: String,
        description: String,
        payloadJson: String
    ): HITLApprovalEntity = withContext(Dispatchers.IO) {
        val approvalId = "appr-${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val expiresAt = now + (24 * 60 * 60 * 1000)

        val rawBase = "$approvalId:${context.tenantId}:$workflowId:$toolName:$now"
        val hmacSignature = CryptoManager.hmacSha256(rawBase)

        val entity = HITLApprovalEntity(
            approvalId = approvalId,
            tenantId = context.tenantId,
            workflowId = workflowId,
            actionType = actionType,
            toolName = toolName,
            riskLevel = riskLevel,
            target = target,
            description = description,
            payloadJson = payloadJson,
            hmacSignature = hmacSignature,
            createdAt = now,
            expiresAt = expiresAt,
            status = ApprovalStatus.PENDING
        )

        database.approvalDao().insertApproval(entity)
        entity
    }

    suspend fun resolveApproval(
        approvalId: String,
        decision: ApprovalStatus,
        supervisorContext: TenantContext
    ): Result<HITLApprovalEntity> = withContext(Dispatchers.IO) {
        val request = database.approvalDao().getApprovalById(approvalId)
            ?: return@withContext Result.failure(IllegalArgumentException("Approval request not found."))

        // 1. Cross-tenant isolation verification
        if (request.tenantId != supervisorContext.tenantId) {
            auditLogger.log(
                tenantId = supervisorContext.tenantId,
                actorId = supervisorContext.userId,
                workflowId = request.workflowId,
                actionType = request.actionType,
                toolName = request.toolName,
                policyVerdict = PolicyVerdict.BLOCKED_BY_POLICY,
                inputHash = CryptoManager.sha256(request.payloadJson),
                details = "Cross-Tenant Access Violation: User attempted to resolve approval outside their tenant scope."
            )
            return@withContext Result.failure(SecurityException("Cross-tenant authorization prohibited."))
        }

        // 2. Role authorization check
        if (supervisorContext.role != TenantRole.ORG_ADMIN && supervisorContext.role != TenantRole.SUPERVISOR) {
            auditLogger.log(
                tenantId = supervisorContext.tenantId,
                actorId = supervisorContext.userId,
                workflowId = request.workflowId,
                actionType = request.actionType,
                toolName = request.toolName,
                policyVerdict = PolicyVerdict.BLOCKED_BY_POLICY,
                inputHash = CryptoManager.sha256(request.payloadJson),
                details = "RBAC Failure: User role '${supervisorContext.role}' lacks authority to resolve HITL gates."
            )
            return@withContext Result.failure(SecurityException("Only SUPERVISOR or ORG_ADMIN can resolve approvals."))
        }

        if (request.status != ApprovalStatus.PENDING) {
            return@withContext Result.failure(IllegalStateException("Approval is already resolved (${request.status})."))
        }

        val updated = request.copy(
            status = decision,
            resolvedBy = supervisorContext.userId,
            resolvedAt = System.currentTimeMillis()
        )
        database.approvalDao().updateApproval(updated)

        // Log to WORM audit ledger
        val verdict = if (decision == ApprovalStatus.APPROVED) PolicyVerdict.PASS else PolicyVerdict.BLOCKED_BY_POLICY
        auditLogger.log(
            tenantId = supervisorContext.tenantId,
            actorId = supervisorContext.userId,
            workflowId = request.workflowId,
            actionType = request.actionType,
            toolName = request.toolName,
            policyVerdict = verdict,
            inputHash = CryptoManager.sha256(request.payloadJson),
            details = "HITL Gate Decision: $decision by supervisor ${supervisorContext.userId} (${supervisorContext.role})."
        )

        _events.emit(ApprovalResolutionEvent.Resolved(updated, decision))
        Result.success(updated)
    }
}
