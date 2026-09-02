package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.ApprovalStatus
import com.example.data.model.WorkflowState
import com.example.ui.components.ApprovalStatusBadge
import com.example.ui.components.HashChip
import com.example.ui.components.MetricCard
import com.example.ui.components.PolicyVerdictBadge
import com.example.ui.components.RiskBadge
import com.example.ui.components.RoleBadge
import com.example.ui.components.WORMIntegrityBanner
import com.example.ui.components.WorkflowStateBadge
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultGold
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AIVaultsViewModel

@Composable
fun DashboardScreen(
    viewModel: AIVaultsViewModel,
    onNavigateToWorkflows: () -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToPolicySim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tenantContext by viewModel.tenantContext.collectAsStateWithLifecycle()
    val workflows by viewModel.workflows.collectAsStateWithLifecycle()
    val allApprovals by viewModel.allApprovals.collectAsStateWithLifecycle()
    val auditRecords by viewModel.auditRecords.collectAsStateWithLifecycle()
    val activeWorkflowsCount by viewModel.activeWorkflowsCount.collectAsStateWithLifecycle()
    val pendingApprovalsCount by viewModel.pendingApprovalsCount.collectAsStateWithLifecycle()
    val blockedAttemptsCount by viewModel.blockedAttemptsCount.collectAsStateWithLifecycle()
    val isChainValid by viewModel.isChainValid.collectAsStateWithLifecycle()
    val isVerifyingChain by viewModel.isVerifyingChain.collectAsStateWithLifecycle()

    var customDomainInput by remember { mutableStateOf("") }
    val presetDomains = listOf("transport-hub.nl", "fintech-group.com", "logistics-nordic.eu", "medicare-express.de")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // WORM Cryptographic Seal Banner
            WORMIntegrityBanner(
                isValid = isChainValid,
                isVerifying = isVerifyingChain,
                onVerifyClick = { viewModel.verifyChainIntegrity() },
                onTamperClick = { viewModel.simulateTamperAttack() },
                onRestoreClick = { viewModel.restoreChain() }
            )
        }

        item {
            // Metrics 2x2 Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Active Workflows",
                        value = "$activeWorkflowsCount",
                        subtitle = "Autonomous agent pipelines",
                        icon = Icons.Default.Bolt,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkflows
                    )
                    MetricCard(
                        title = "HITL Approvals",
                        value = "$pendingApprovalsCount",
                        subtitle = if (pendingApprovalsCount > 0) "Pending supervisor review" else "All gates cleared",
                        icon = Icons.Default.PendingActions,
                        accentColor = if (pendingApprovalsCount > 0) VaultGold else EmeraldSafe,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToApprovals
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "WORM Audit Ledger",
                        value = "${auditRecords.size} Blocks",
                        subtitle = if (isChainValid == true) "SHA-256 Validated" else "Tamper Alert",
                        icon = Icons.Default.Lock,
                        accentColor = if (isChainValid == true) EmeraldSafe else CoralAlert,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLedger
                    )
                    MetricCard(
                        title = "Blocked Violations",
                        value = "$blockedAttemptsCount",
                        subtitle = "Fail-Closed Gatekeepers",
                        icon = Icons.Default.Security,
                        accentColor = CoralAlert,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPolicySim
                    )
                }
            }
        }

        // Quick Launch Discovery Pipeline Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_prospect_card"),
                colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, CyberCyan.copy(alpha = 0.3f)))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Autonomous Prospect Pipeline",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Run AI detection, fit scoring, draft proposal & HITL gate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset domain chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetDomains.forEach { domain ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VaultNavyCardLight)
                                    .border(1.dp, VaultBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        customDomainInput = domain
                                        viewModel.startWorkflow(domain)
                                        onNavigateToWorkflows()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("preset_chip_$domain")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(domain, color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom URL input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customDomainInput,
                            onValueChange = { customDomainInput = it },
                            placeholder = { Text("e.g. enterprise-target.com", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_domain_input"),
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
                                if (customDomainInput.isNotBlank()) {
                                    viewModel.startWorkflow(customDomainInput)
                                    customDomainInput = ""
                                    onNavigateToWorkflows()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_workflow_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Workflows Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Pipelines (${workflows.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All →",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToWorkflows() }
                )
            }
        }

        items(workflows.take(3)) { workflow ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectWorkflow(workflow.workflowId)
                        onNavigateToWorkflows()
                    }
                    .testTag("dashboard_workflow_${workflow.workflowId}"),
                colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, CyberCyan.copy(alpha = 0.15f)))),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workflow.targetDomain,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        WorkflowStateBadge(state = workflow.state)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (workflow.aiDetected) "AI Bot Detected: ${workflow.aiProviders.firstOrNull() ?: "Yes"}" else "No AI detected (High Opportunity)",
                            fontSize = 11.sp,
                            color = if (workflow.aiDetected) VaultGold else EmeraldSafe
                        )
                        if (workflow.qualificationScore > 0) {
                            Text(
                                text = "Fit: ${workflow.qualificationScore}/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                    }

                    if (workflow.state == WorkflowState.WAITING_FOR_HITL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(VaultGold.copy(alpha = 0.12f))
                                .border(1.dp, VaultGold.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .clickable { onNavigateToApprovals() }
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PendingActions, contentDescription = null, tint = VaultGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Awaiting Supervisor Authorization", color = VaultGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VaultGold, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Live SIEM Audit Trail Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live WORM Cryptographic Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Full Audit Chain →",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToLedger() }
                )
            }
        }

        items(auditRecords.takeLast(4).reversed()) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLedger() }
                    .testTag("dashboard_audit_log_${log.logId}"),
                colors = CardDefaults.cardColors(containerColor = if (log.isTampered) CoralAlert.copy(alpha = 0.1f) else VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            if (log.isTampered) CoralAlert else VaultBorder,
                            if (log.isTampered) CoralAlert.copy(alpha = 0.4f) else VaultBorder
                        )
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.toolName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "by ${log.actorId}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PolicyVerdictBadge(verdict = runCatching { com.example.data.model.PolicyVerdict.valueOf(log.policyVerdict) }.getOrDefault(com.example.data.model.PolicyVerdict.PASS))
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.details,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HashChip(label = "Hash", hash = log.currentHash, isTampered = log.isTampered)
                        Text(
                            text = log.timestamp.takeLast(12).take(8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
