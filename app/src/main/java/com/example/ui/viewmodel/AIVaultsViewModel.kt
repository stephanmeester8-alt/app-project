package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AuditRecordEntity
import com.example.data.local.HITLApprovalEntity
import com.example.data.local.WorkflowEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.PolicyEvaluationResult
import com.example.data.model.PolicyVerdict
import com.example.data.model.RiskLevel
import com.example.data.model.TenantContext
import com.example.data.model.TenantRole
import com.example.data.model.ToolActionType
import com.example.data.model.WorkflowState
import com.example.domain.audit.AuditLogger
import com.example.domain.hitl.ApprovalEngine
import com.example.domain.orchestrator.AgentOrchestrator
import com.example.domain.policy.EnterpriseToolRegistry
import com.example.domain.policy.PolicyEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PolicySimulationUiState(
    val selectedTool: String = "email_outreach_adapter",
    val actionType: ToolActionType = ToolActionType.EXTERNAL_SIDE_EFFECT,
    val riskLevel: RiskLevel = RiskLevel.HIGH,
    val payload: String = "{\"to\": \"ceo@target-corp.com\", \"subject\": \"AIVaults Enterprise Integration\", \"body\": \"Automate customer service intake with high assurance.\"}",
    val simulatedRole: TenantRole = TenantRole.OPERATOR,
    val payloadSizeKb: Int = 4,
    val isRunning: Boolean = false,
    val result: PolicyEvaluationResult? = null
)

class AIVaultsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val auditLogger = AuditLogger(database)
    val policyEngine = PolicyEngine(auditLogger)
    val approvalEngine = ApprovalEngine(database, auditLogger)
    val toolRegistry = EnterpriseToolRegistry(policyEngine, approvalEngine, auditLogger)
    val orchestrator = AgentOrchestrator(database, toolRegistry, approvalEngine, auditLogger, viewModelScope)

    private val _tenantContext = MutableStateFlow(
        TenantContext(
            tenantId = "tenant-enterprise-889",
            userId = "usr-supervisor-admin",
            role = TenantRole.SUPERVISOR,
            rateLimitPerMinute = 60,
            maxContextBufferSizeKb = 32
        )
    )
    val tenantContext: StateFlow<TenantContext> = _tenantContext.asStateFlow()

    val workflows: StateFlow<List<WorkflowEntity>> = database.workflowDao().getAllWorkflowsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApprovals: StateFlow<List<HITLApprovalEntity>> = database.approvalDao().getAllApprovalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditRecords: StateFlow<List<AuditRecordEntity>> = database.auditDao().getAllRecordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkflowsCount: StateFlow<Int> = database.workflowDao().getActiveWorkflowsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingApprovalsCount: StateFlow<Int> = database.approvalDao().getPendingCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val blockedAttemptsCount: StateFlow<Int> = database.auditDao().getBlockedAttemptsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isChainValid = MutableStateFlow<Boolean?>(true)
    val isChainValid: StateFlow<Boolean?> = _isChainValid.asStateFlow()

    private val _isVerifyingChain = MutableStateFlow(false)
    val isVerifyingChain: StateFlow<Boolean> = _isVerifyingChain.asStateFlow()

    private val _selectedWorkflowId = MutableStateFlow<String?>(null)
    val selectedWorkflowId: StateFlow<String?> = _selectedWorkflowId.asStateFlow()

    private val _policySimState = MutableStateFlow(PolicySimulationUiState())
    val policySimState: StateFlow<PolicySimulationUiState> = _policySimState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        seedInitialDataIfEmpty()
        verifyChainIntegrity()
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectWorkflow(id: String?) {
        _selectedWorkflowId.value = id
    }

    fun updateRole(newRole: TenantRole) {
        _tenantContext.value = _tenantContext.value.copy(
            role = newRole,
            userId = when (newRole) {
                TenantRole.ORG_ADMIN -> "usr-admin-root"
                TenantRole.SUPERVISOR -> "usr-supervisor-lead"
                TenantRole.OPERATOR -> "usr-analyst-01"
                TenantRole.AUDITOR -> "usr-compliance-auditor"
            }
        )
        _toastMessage.value = "Active identity updated: ${_tenantContext.value.userId} (${newRole.name})"
    }

    fun updateTenantId(newTenantId: String) {
        _tenantContext.value = _tenantContext.value.copy(tenantId = newTenantId)
        _toastMessage.value = "Switched to tenant: $newTenantId"
    }

    fun startWorkflow(domain: String) {
        viewModelScope.launch {
            val cleanDomain = domain.trim().removePrefix("https://").removePrefix("http://")
            val wf = orchestrator.startProspectWorkflow(cleanDomain, _tenantContext.value)
            _selectedWorkflowId.value = wf.workflowId
            _toastMessage.value = "Launched autonomous prospect run for $cleanDomain"
            verifyChainIntegrity()
        }
    }

    fun resolveApproval(approvalId: String, decision: ApprovalStatus) {
        viewModelScope.launch {
            val result = approvalEngine.resolveApproval(approvalId, decision, _tenantContext.value)
            if (result.isSuccess) {
                _toastMessage.value = "Approval ${if (decision == ApprovalStatus.APPROVED) "GRANTED" else "REJECTED"} for $approvalId"
            } else {
                _toastMessage.value = "Resolution Failed: ${result.exceptionOrNull()?.message}"
            }
            verifyChainIntegrity()
        }
    }

    fun verifyChainIntegrity() {
        viewModelScope.launch {
            _isVerifyingChain.value = true
            delay(600) // Visual verification scan simulation
            _isChainValid.value = auditLogger.verifyIntegrity()
            _isVerifyingChain.value = false
        }
    }

    fun simulateTamperAttack() {
        viewModelScope.launch {
            val done = auditLogger.simulateTamperAttack()
            if (done) {
                _isChainValid.value = auditLogger.verifyIntegrity()
                _toastMessage.value = "⚠️ Cryptographic alert: Tamper detected in block chain!"
            }
        }
    }

    fun restoreChain() {
        viewModelScope.launch {
            auditLogger.restoreChain()
            _isChainValid.value = auditLogger.verifyIntegrity()
            _toastMessage.value = "✅ WORM cryptographic chain sealed and restored."
        }
    }

    fun updatePolicySim(update: PolicySimulationUiState.() -> PolicySimulationUiState) {
        _policySimState.value = _policySimState.value.update()
    }

    fun runPolicySimulation() {
        viewModelScope.launch {
            _policySimState.value = _policySimState.value.copy(isRunning = true)
            val state = _policySimState.value
            val simContext = _tenantContext.value.copy(
                role = state.simulatedRole,
                maxContextBufferSizeKb = 32
            )

            val simPayload = if (state.payloadSizeKb > 32) {
                state.payload + " ".repeat((state.payloadSizeKb - 32) * 1024)
            } else {
                state.payload
            }

            val result = policyEngine.evaluate(
                toolName = state.selectedTool,
                actionType = state.actionType,
                riskLevel = state.riskLevel,
                requiresHITL = state.actionType == ToolActionType.EXTERNAL_SIDE_EFFECT || state.actionType == ToolActionType.DESTRUCTIVE,
                payloadJson = simPayload,
                context = simContext,
                workflowId = "sim-wf-${System.currentTimeMillis()}"
            )

            delay(400)
            _policySimState.value = _policySimState.value.copy(
                isRunning = false,
                result = result
            )
            verifyChainIntegrity()
        }
    }

    private fun seedInitialDataIfEmpty() {
        viewModelScope.launch {
            val recordsCount = database.auditDao().getCount()
            if (recordsCount == 0) {
                // Initialize Genesis WORM Audit Block
                auditLogger.log(
                    tenantId = "tenant-enterprise-889",
                    actorId = "SYSTEM_RUNTIME",
                    workflowId = "wf-genesis-000",
                    actionType = ToolActionType.READ,
                    toolName = "system_kernel_boot",
                    policyVerdict = PolicyVerdict.PASS,
                    inputHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    details = "AIVaultsAI Enterprise Kernel 2.5 initialized with fail-closed security policies & WORM hash chain."
                )

                // Start an initial sample workflow
                val sampleWorkflow = orchestrator.startProspectWorkflow("transport-hub.nl", _tenantContext.value)
                _selectedWorkflowId.value = sampleWorkflow.workflowId
            }
        }
    }
}
