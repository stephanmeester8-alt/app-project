package com.example.domain.orchestrator

import com.example.data.local.AppDatabase
import com.example.data.local.WorkflowEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.TenantContext
import com.example.data.model.WorkflowState
import com.example.domain.audit.AuditLogger
import com.example.domain.hitl.ApprovalEngine
import com.example.domain.hitl.ApprovalResolutionEvent
import com.example.domain.policy.EnterpriseToolRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AgentOrchestrator(
    private val database: AppDatabase,
    private val toolRegistry: EnterpriseToolRegistry,
    private val approvalEngine: ApprovalEngine,
    private val auditLogger: AuditLogger,
    private val scope: CoroutineScope
) {

    init {
        // Listen for HITL approval/rejection resolutions to resume halted workflows
        scope.launch {
            approvalEngine.events.collectLatest { event ->
                when (event) {
                    is ApprovalResolutionEvent.Resolved -> {
                        val workflow = database.workflowDao().getWorkflowById(event.approval.workflowId)
                        if (workflow != null && workflow.state == WorkflowState.WAITING_FOR_HITL) {
                            if (event.decision == ApprovalStatus.APPROVED) {
                                database.workflowDao().updateWorkflow(
                                    workflow.copy(
                                        state = WorkflowState.EXECUTING,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                                delay(1200) // Realistic dispatch pacing
                                database.workflowDao().updateWorkflow(
                                    workflow.copy(
                                        state = WorkflowState.COMPLETED,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                database.workflowDao().updateWorkflow(
                                    workflow.copy(
                                        state = WorkflowState.BLOCKED_BY_POLICY,
                                        error = "Outreach blocked: Rejected by supervisor during HITL review.",
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun startProspectWorkflow(
        domain: String,
        context: TenantContext
    ): WorkflowEntity = withContext(Dispatchers.IO) {
        val workflowId = "wf-${UUID.randomUUID()}"
        val initialWorkflow = WorkflowEntity(
            workflowId = workflowId,
            tenantId = context.tenantId,
            targetDomain = domain,
            state = WorkflowState.INITIALIZED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        database.workflowDao().insertWorkflow(initialWorkflow)

        // Launch asynchronous pipeline step by step
        scope.launch(Dispatchers.IO) {
            executeWorkflowPipeline(workflowId, domain, context)
        }

        initialWorkflow
    }

    private suspend fun executeWorkflowPipeline(
        workflowId: String,
        domain: String,
        context: TenantContext
    ) {
        var workflow = database.workflowDao().getWorkflowById(workflowId) ?: return

        // STEP 1: Researching
        workflow = workflow.copy(state = WorkflowState.RESEARCHING, updatedAt = System.currentTimeMillis())
        database.workflowDao().updateWorkflow(workflow)
        delay(1000)

        val researchResult = toolRegistry.executeWebsiteResearch(
            url = domain,
            context = context,
            workflowId = workflowId
        )

        if (!researchResult.success || researchResult.result == null) {
            database.workflowDao().updateWorkflow(
                workflow.copy(
                    state = WorkflowState.FAILED,
                    error = researchResult.error ?: "Website intelligence failed",
                    updatedAt = System.currentTimeMillis()
                )
            )
            return
        }

        val data = researchResult.result

        // STEP 2: AI Signature Detection
        workflow = workflow.copy(
            state = WorkflowState.AI_DETECTION,
            payloadSizeKb = data.payloadSizeKb,
            headings = data.headings,
            contactEmails = data.contactEmails,
            contactPhones = data.contactPhones,
            hasContactForm = data.hasContactForm,
            aiDetected = data.aiDetected,
            aiProviders = data.aiProviders,
            updatedAt = System.currentTimeMillis()
        )
        database.workflowDao().updateWorkflow(workflow)
        delay(1200)

        // STEP 3: Qualifying & Fit Scoring
        workflow = workflow.copy(
            state = WorkflowState.QUALIFYING,
            qualificationScore = data.opportunityScore,
            updatedAt = System.currentTimeMillis()
        )
        database.workflowDao().updateWorkflow(workflow)
        delay(1000)

        // STEP 4: Proposal Generation & Outreach Draft
        val cleanName = domain.split(".").first().uppercase()
        val subject = "Automatisering van klant- en service-intakes voor $cleanName"
        val body = """
            Geachte directie van $cleanName,
            
            Tijdens onze autonome analyse van uw online portaal (${data.domain}) constateerden wij dat uw intake- en klantprocessen (${data.headings.take(2).joinToString(", ")}) nog aanzienlijke handmatige capaciteit vergen.
            
            AIVaultsAI implementeert een veilige, fail-closed autonome AI-medewerker die:
            1. 24/7 telefonische & web-intakes foutloos afhandelt.
            2. Direct synchroniseert met uw ERP en WORM audit-trails waarborgt.
            3. Uitgevoerd wordt onder strikte Human-In-The-Loop supervisie.
            
            Graag plannen wij een korte demonstratie van 15 minuten.
            
            Met vriendelijke groet,
            AIVaults Enterprise Agent Runtime
        """.trimIndent()

        workflow = workflow.copy(
            draftSubject = subject,
            draftBody = body,
            updatedAt = System.currentTimeMillis()
        )
        database.workflowDao().updateWorkflow(workflow)

        // STEP 5: Execution Gate (HITL approval trigger)
        workflow = workflow.copy(
            state = WorkflowState.WAITING_FOR_HITL,
            updatedAt = System.currentTimeMillis()
        )
        database.workflowDao().updateWorkflow(workflow)

        val outreachResult = toolRegistry.executeEmailOutreach(
            to = data.contactEmails.firstOrNull() ?: "contact@$domain",
            subject = subject,
            body = body,
            context = context,
            workflowId = workflowId
        )

        if (outreachResult.hitlRequired && outreachResult.approval != null) {
            workflow = workflow.copy(
                pendingApprovalId = outreachResult.approval.approvalId,
                updatedAt = System.currentTimeMillis()
            )
            database.workflowDao().updateWorkflow(workflow)
        } else if (!outreachResult.success) {
            workflow = workflow.copy(
                state = WorkflowState.FAILED,
                error = outreachResult.error,
                updatedAt = System.currentTimeMillis()
            )
            database.workflowDao().updateWorkflow(workflow)
        }
    }
}
