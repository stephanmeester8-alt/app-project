package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.PolicyVerdict
import com.example.data.model.RiskLevel
import com.example.data.model.TenantRole
import com.example.data.model.ToolActionType
import com.example.ui.components.HashChip
import com.example.ui.components.PolicyVerdictBadge
import com.example.ui.components.RiskBadge
import com.example.ui.components.RoleBadge
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultGold
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.viewmodel.AIVaultsViewModel

@Composable
fun PolicySimulatorScreen(
    viewModel: AIVaultsViewModel,
    modifier: Modifier = Modifier
) {
    val simState by viewModel.policySimState.collectAsStateWithLifecycle()
    val tenantContext by viewModel.tenantContext.collectAsStateWithLifecycle()

    val toolOptions = listOf(
        "email_outreach_adapter",
        "assistant_website_research",
        "database_mutation_adapter",
        "custom_agent_adapter"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Simulator Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("policy_simulator_header"),
                colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, CyberCyan.copy(alpha = 0.35f)))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Deterministic Policy Simulator",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Test payload bounds, RBAC permissions, and fail-closed gates in real time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Policy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tool Selection
                    Text("Target Tool Definition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        toolOptions.forEach { tool ->
                            val isSelected = simState.selectedTool == tool
                            val shortName = tool.split("_").first().replaceFirstChar { it.uppercase() }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else VaultNavyCardLight)
                                    .border(1.dp, if (isSelected) CyberCyan else VaultBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val newAction = when (tool) {
                                            "assistant_website_research" -> ToolActionType.READ
                                            "email_outreach_adapter" -> ToolActionType.EXTERNAL_SIDE_EFFECT
                                            "database_mutation_adapter" -> ToolActionType.DESTRUCTIVE
                                            else -> ToolActionType.WRITE
                                        }
                                        val newRisk = when (tool) {
                                            "assistant_website_research" -> RiskLevel.LOW
                                            "email_outreach_adapter" -> RiskLevel.HIGH
                                            "database_mutation_adapter" -> RiskLevel.CRITICAL
                                            else -> RiskLevel.MEDIUM
                                        }
                                        viewModel.updatePolicySim {
                                            copy(selectedTool = tool, actionType = newAction, riskLevel = newRisk)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Type Selection
                    Text("Action Type & Risk Classification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ToolActionType.values().forEach { action ->
                            val isSelected = simState.actionType == action
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) VaultGold.copy(alpha = 0.2f) else VaultNavyCardLight)
                                    .border(1.dp, if (isSelected) VaultGold else VaultBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.updatePolicySim { copy(actionType = action) } }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = action.name.take(6),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) VaultGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Simulated Actor Role
                    Text("Simulated Caller Role (RBAC)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TenantRole.values().forEach { role ->
                            val isSelected = simState.simulatedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) EmeraldSafe.copy(alpha = 0.2f) else VaultNavyCardLight)
                                    .border(1.dp, if (isSelected) EmeraldSafe else VaultBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.updatePolicySim { copy(simulatedRole = role) } }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.name.take(6),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldSafe else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Payload Buffer Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payload Size:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${simState.payloadSizeKb} KB ${if (simState.payloadSizeKb > 32) "(BREACHES 32KB LIMIT)" else "(Compliant)"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (simState.payloadSizeKb > 32) CoralAlert else EmeraldSafe
                        )
                    }
                    Slider(
                        value = simState.payloadSizeKb.toFloat(),
                        onValueChange = { viewModel.updatePolicySim { copy(payloadSizeKb = it.toInt()) } },
                        valueRange = 1f..64f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (simState.payloadSizeKb > 32) CoralAlert else CyberCyan,
                            activeTrackColor = if (simState.payloadSizeKb > 32) CoralAlert else CyberCyan,
                            inactiveTrackColor = VaultBorder
                        )
                    )

                    // Execute Simulation Button
                    Button(
                        onClick = { viewModel.runPolicySimulation() },
                        enabled = !simState.isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_policy_sim_button")
                    ) {
                        if (simState.isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Evaluating Policy Engine...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Evaluate Policy Engine Rules", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Policy Evaluation Result Box
        if (simState.result != null) {
            val res = simState.result!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_evaluation_result_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = when (res.verdict) {
                            PolicyVerdict.PASS -> EmeraldSafe.copy(alpha = 0.12f)
                            PolicyVerdict.HALTED_FOR_HITL -> VaultGold.copy(alpha = 0.15f)
                            PolicyVerdict.BLOCKED_BY_POLICY, PolicyVerdict.EXECUTION_FAILED -> CoralAlert.copy(alpha = 0.15f)
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                when (res.verdict) {
                                    PolicyVerdict.PASS -> EmeraldSafe
                                    PolicyVerdict.HALTED_FOR_HITL -> VaultGold
                                    else -> CoralAlert
                                },
                                VaultBorder
                            )
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Policy Engine Verdict",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            PolicyVerdictBadge(verdict = res.verdict)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = res.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HashChip(label = "Input Hash", hash = res.inputHash)
                            Text(
                                text = if (res.requiresHITL) "HITL ESCALATION" else if (res.allowed) "DIRECT PASS" else "BLOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (res.requiresHITL) VaultGold else if (res.allowed) EmeraldSafe else CoralAlert
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
