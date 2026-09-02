package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WorkflowEntity
import com.example.data.model.WorkflowState
import com.example.ui.components.WorkflowStateBadge
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultGold
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.viewmodel.AIVaultsViewModel

@Composable
fun WorkflowsScreen(
    viewModel: AIVaultsViewModel,
    onNavigateToApprovals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workflows by viewModel.workflows.collectAsStateWithLifecycle()
    val selectedWorkflowId by viewModel.selectedWorkflowId.collectAsStateWithLifecycle()

    var domainInput by remember { mutableStateOf("") }

    val activeWorkflow = workflows.find { it.workflowId == selectedWorkflowId } ?: workflows.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Pipeline Launcher Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pipeline_runner_card"),
                colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, CyberCyan.copy(alpha = 0.3f)))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Orchestrate Prospect Discovery Run",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter a corporate domain to launch the multi-stage autonomous agent pipeline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = domainInput,
                            onValueChange = { domainInput = it },
                            placeholder = { Text("e.g. logistics-nordic.eu", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workflow_domain_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = VaultBorder,
                                focusedContainerColor = VaultNavyCardLight,
                                unfocusedContainerColor = VaultNavyCardLight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (domainInput.isNotBlank()) {
                                    viewModel.startWorkflow(domainInput)
                                    domainInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("launch_pipeline_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Launch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Workflow Detail / DAG visualizer
        if (activeWorkflow != null) {
            item {
                WorkflowDetailCard(
                    workflow = activeWorkflow,
                    onNavigateToApprovals = onNavigateToApprovals
                )
            }
        }

        // List of all workflows
        item {
            Text(
                text = "Execution History (${workflows.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        items(workflows) { workflow ->
            val isSelected = workflow.workflowId == activeWorkflow?.workflowId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectWorkflow(workflow.workflowId) }
                    .testTag("workflow_item_${workflow.workflowId}"),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) VaultNavyCardLight else VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            if (isSelected) CyberCyan else VaultBorder,
                            if (isSelected) CyberCyan.copy(alpha = 0.3f) else VaultBorder
                        )
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = workflow.targetDomain,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        WorkflowStateBadge(state = workflow.state)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tenant: ${workflow.tenantId}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (workflow.qualificationScore > 0) {
                            Text(
                                text = "Score: ${workflow.qualificationScore}/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (workflow.qualificationScore > 70) EmeraldSafe else VaultGold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WorkflowDetailCard(
    workflow: WorkflowEntity,
    onNavigateToApprovals: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workflow_detail_card"),
        colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, CyberCyan.copy(alpha = 0.4f)))),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pipeline: ${workflow.targetDomain}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${workflow.workflowId}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                WorkflowStateBadge(state = workflow.state)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // State Machine DAG Visualizer
            Text(
                text = "Autonomous Execution DAG",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val stages = listOf(
                Pair(WorkflowState.INITIALIZED, "Init"),
                Pair(WorkflowState.RESEARCHING, "Research"),
                Pair(WorkflowState.AI_DETECTION, "AI Scan"),
                Pair(WorkflowState.QUALIFYING, "Qualify"),
                Pair(WorkflowState.WAITING_FOR_HITL, "HITL Gate"),
                Pair(WorkflowState.EXECUTING, "Dispatch"),
                Pair(WorkflowState.COMPLETED, "Done")
            )

            val currentStateIndex = when (workflow.state) {
                WorkflowState.INITIALIZED -> 0
                WorkflowState.RESEARCHING -> 1
                WorkflowState.AI_DETECTION -> 2
                WorkflowState.QUALIFYING -> 3
                WorkflowState.WAITING_FOR_HITL -> 4
                WorkflowState.EXECUTING -> 5
                WorkflowState.COMPLETED -> 6
                WorkflowState.FAILED, WorkflowState.BLOCKED_BY_POLICY -> 4
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { index, (state, label) ->
                    val isDone = index < currentStateIndex || workflow.state == WorkflowState.COMPLETED
                    val isCurrent = index == currentStateIndex && workflow.state != WorkflowState.COMPLETED
                    val isFailed = (workflow.state == WorkflowState.FAILED || workflow.state == WorkflowState.BLOCKED_BY_POLICY) && index == 4

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isFailed -> CoralAlert
                                        isDone -> EmeraldSafe
                                        isCurrent -> CyberCyan
                                        else -> VaultNavyCardLight
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isCurrent) CyberCyan else VaultBorder,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.Black
                                )
                            } else if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            } else if (isFailed) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text("${index + 1}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Detection & Intelligence Card
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultNavyCardLight),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, VaultBorder))),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = if (workflow.aiDetected) VaultGold else EmeraldSafe, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Signature Intelligence", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(
                            text = if (workflow.aiDetected) "AI DETECTED" else "NO AI (PRIME TARGET)",
                            color = if (workflow.aiDetected) VaultGold else EmeraldSafe,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (workflow.aiProviders.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Detected Providers: ${workflow.aiProviders.joinToString(", ")}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (workflow.contactEmails.isNotEmpty() || workflow.contactPhones.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (workflow.contactEmails.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(workflow.contactEmails.first(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (workflow.contactPhones.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(workflow.contactPhones.first(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Draft Proposal Preview
            if (!workflow.draftSubject.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = VaultNavyCardLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Generated Outreach Proposal:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = workflow.draftSubject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = workflow.draftBody?.take(180) + "...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pending Approval Callout
            if (workflow.state == WorkflowState.WAITING_FOR_HITL) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNavigateToApprovals,
                    colors = ButtonDefaults.buttonColors(containerColor = VaultGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_approval_button")
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Review Pending HITL Gate (${workflow.pendingApprovalId ?: "Action"})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
