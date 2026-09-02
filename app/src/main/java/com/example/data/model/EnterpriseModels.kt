package com.example.data.model

import java.util.Date

enum class TenantRole {
    ORG_ADMIN,
    SUPERVISOR,
    OPERATOR,
    AUDITOR
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ToolActionType {
    READ,
    WRITE,
    EXTERNAL_SIDE_EFFECT,
    DESTRUCTIVE
}

enum class WorkflowState {
    INITIALIZED,
    RESEARCHING,
    AI_DETECTION,
    QUALIFYING,
    WAITING_FOR_HITL,
    EXECUTING,
    COMPLETED,
    FAILED,
    BLOCKED_BY_POLICY
}

enum class PolicyVerdict {
    PASS,
    HALTED_FOR_HITL,
    BLOCKED_BY_POLICY,
    EXECUTION_FAILED
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}

data class TenantContext(
    val tenantId: String = "tenant-enterprise-889",
    val userId: String = "usr-analyst-01",
    val role: TenantRole = TenantRole.OPERATOR,
    val rateLimitPerMinute: Int = 30,
    val maxContextBufferSizeKb: Int = 32,
    val allowedDomains: List<String> = listOf("transport-hub.nl", "fintech-group.com", "logistics-nordic.eu", "medicare-express.de")
)

data class WebsiteResearchResult(
    val domain: String,
    val payloadSizeKb: Double,
    val headings: List<String>,
    val contactEmails: List<String>,
    val contactPhones: List<String>,
    val hasContactForm: Boolean,
    val aiDetected: Boolean,
    val aiProviders: List<String>,
    val rawMatches: List<String>,
    val opportunityScore: Int,
    val rawSummary: String
)

data class DraftOutreach(
    val recipient: String,
    val subject: String,
    val body: String,
    val valueProposition: String
)

data class PolicyEvaluationResult(
    val allowed: Boolean,
    val requiresHITL: Boolean,
    val verdict: PolicyVerdict,
    val reason: String,
    val inputHash: String
)
