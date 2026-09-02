package com.example.domain.policy

import com.example.data.model.PolicyEvaluationResult
import com.example.data.model.PolicyVerdict
import com.example.data.model.RiskLevel
import com.example.data.model.TenantContext
import com.example.data.model.TenantRole
import com.example.data.model.ToolActionType
import com.example.domain.audit.AuditLogger
import com.example.domain.crypto.CryptoManager
import java.util.concurrent.ConcurrentHashMap

data class SlidingWindowUsage(var count: Int, var windowStart: Long)

class PolicyEngine(private val auditLogger: AuditLogger) {
    private val tenantUsageMap = ConcurrentHashMap<String, SlidingWindowUsage>()

    suspend fun evaluate(
        toolName: String,
        actionType: ToolActionType,
        riskLevel: RiskLevel,
        requiresHITL: Boolean,
        payloadJson: String,
        context: TenantContext,
        workflowId: String
    ): PolicyEvaluationResult {
        val inputHash = CryptoManager.sha256(payloadJson)

        // 1. Fail-closed: Validate Tenant Context
        if (context.tenantId.isBlank() || context.userId.isBlank()) {
            auditLogger.log(
                tenantId = if (context.tenantId.isBlank()) "UNKNOWN" else context.tenantId,
                actorId = if (context.userId.isBlank()) "ANONYMOUS" else context.userId,
                workflowId = workflowId,
                actionType = actionType,
                toolName = toolName,
                policyVerdict = PolicyVerdict.BLOCKED_BY_POLICY,
                inputHash = inputHash,
                details = "Security Fault: Missing or invalid multi-tenant security context. Fail-closed trigger."
            )
            return PolicyEvaluationResult(
                allowed = false,
                requiresHITL = false,
                verdict = PolicyVerdict.BLOCKED_BY_POLICY,
                reason = "Invalid or missing tenant security context.",
                inputHash = inputHash
            )
        }

        // 2. Buffer Size & Payload Bounds Check
        val payloadSizeKb = payloadJson.toByteArray(Charsets.UTF_8).size / 1024.0
        if (payloadSizeKb > context.maxContextBufferSizeKb) {
            auditLogger.log(
                tenantId = context.tenantId,
                actorId = context.userId,
                workflowId = workflowId,
                actionType = actionType,
                toolName = toolName,
                policyVerdict = PolicyVerdict.BLOCKED_BY_POLICY,
                inputHash = inputHash,
                details = "Buffer Overflow Protection: Payload size ${"%.2f".format(payloadSizeKb)}KB exceeds maximum allowed limit (${context.maxContextBufferSizeKb}KB)."
            )
            return PolicyEvaluationResult(
                allowed = false,
                requiresHITL = false,
                verdict = PolicyVerdict.BLOCKED_BY_POLICY,
                reason = "Payload exceeds maximum ${context.maxContextBufferSizeKb}KB context buffer limit.",
                inputHash = inputHash
            )
        }

        // 3. Sliding Window Rate Limiting per Tenant
        val now = System.currentTimeMillis()
        val usage = tenantUsageMap.getOrPut(context.tenantId) { SlidingWindowUsage(0, now) }
        synchronized(usage) {
            if (now - usage.windowStart > 60_000) {
                usage.count = 0
                usage.windowStart = now
            }
            usage.count++
            if (usage.count > context.rateLimitPerMinute) {
                return@synchronized
            }
        }

        if (usage.count > context.rateLimitPerMinute) {
            auditLogger.log(
                tenantId = context.tenantId,
                actorId = context.userId,
                workflowId = workflowId,
                actionType = actionType,
                toolName = toolName,
                policyVerdict = PolicyVerdict.BLOCKED_BY_POLICY,
                inputHash = inputHash,
                details = "Rate Limit Breach: Tenant exceeded ${context.rateLimitPerMinute} requests/min threshold (${usage.count} hits)."
            )
            return PolicyEvaluationResult(
                allowed = false,
                requiresHITL = false,
                verdict = PolicyVerdict.BLOCKED_BY_POLICY,
                reason = "Tenant rate limit breached (${usage.count}/${context.rateLimitPerMinute} req/min).",
                inputHash = inputHash
            )
        }

        // 4. Role Authorization for Destructive Actions
        if (actionType == ToolActionType.DESTRUCTIVE && context.role != TenantRole.ORG_ADMIN) {
            auditLogger.log(
                tenantId = context.tenantId,
                actorId = context.userId,
                workflowId = workflowId,
                actionType = actionType,
                toolName = toolName,
                policyVerdict = PolicyVerdict.BLOCKED_BY_POLICY,
                inputHash = inputHash,
                details = "RBAC Policy Violation: Destructive tool actions strictly require ORG_ADMIN privileges (Current: ${context.role})."
            )
            return PolicyEvaluationResult(
                allowed = false,
                requiresHITL = false,
                verdict = PolicyVerdict.BLOCKED_BY_POLICY,
                reason = "Destructive tools require ORG_ADMIN role.",
                inputHash = inputHash
            )
        }

        // 5. Risk Classification & Mandatory Human-In-The-Loop (HITL) Gatekeepers
        if (actionType == ToolActionType.EXTERNAL_SIDE_EFFECT || actionType == ToolActionType.DESTRUCTIVE || requiresHITL) {
            auditLogger.log(
                tenantId = context.tenantId,
                actorId = context.userId,
                workflowId = workflowId,
                actionType = actionType,
                toolName = toolName,
                policyVerdict = PolicyVerdict.HALTED_FOR_HITL,
                inputHash = inputHash,
                details = "Execution Gatekeeper: Action type $actionType ($riskLevel) halted. Mandatory human approval required."
            )
            return PolicyEvaluationResult(
                allowed = true,
                requiresHITL = true,
                verdict = PolicyVerdict.HALTED_FOR_HITL,
                reason = "Action requires Human-In-The-Loop (HITL) supervisor authorization before side-effect dispatch.",
                inputHash = inputHash
            )
        }

        // 6. Safe bounded READ operations PASS directly
        auditLogger.log(
            tenantId = context.tenantId,
            actorId = context.userId,
            workflowId = workflowId,
            actionType = actionType,
            toolName = toolName,
            policyVerdict = PolicyVerdict.PASS,
            inputHash = inputHash,
            details = "Policy Engine PASS: Bounded $actionType operation verified and authorized."
        )

        return PolicyEvaluationResult(
            allowed = true,
            requiresHITL = false,
            verdict = PolicyVerdict.PASS,
            reason = "Operation verified and compliant with enterprise policies.",
            inputHash = inputHash
        )
    }
}
