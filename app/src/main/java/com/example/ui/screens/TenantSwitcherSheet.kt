package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TenantRole
import com.example.ui.components.RoleBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.viewmodel.AIVaultsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantSwitcherSheet(
    viewModel: AIVaultsViewModel,
    onDismiss: () -> Unit
) {
    val tenantContext by viewModel.tenantContext.collectAsStateWithLifecycle()
    var tenantInput by remember { mutableStateOf(tenantContext.tenantId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = VaultNavyCard,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tenant & Identity Context",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Tenant ID Configuration
            Text("Multi-Tenant Boundary Scope", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tenantInput,
                    onValueChange = { tenantInput = it },
                    modifier = Modifier.weight(1f).testTag("tenant_id_input"),
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
                        if (tenantInput.isNotBlank()) {
                            viewModel.updateTenantId(tenantInput.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }

            // Preset Tenants
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("tenant-enterprise-889", "tenant-healthcare-012", "tenant-fintech-994").forEach { tId ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VaultNavyCardLight)
                            .border(1.dp, if (tenantContext.tenantId == tId) CyberCyan else VaultBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                tenantInput = tId
                                viewModel.updateTenantId(tId)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(tId.takeLast(10), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = CyberCyan)
                    }
                }
            }

            // Role Switcher
            Text("Active Role-Based Access Control (RBAC)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TenantRole.values().forEach { role ->
                    val isSelected = tenantContext.role == role
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateRole(role)
                            }
                            .testTag("select_role_${role.name}"),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) VaultNavyCardLight else VaultNavyCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) CyberCyan else VaultBorder)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RoleBadge(role = role)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = when (role) {
                                            TenantRole.ORG_ADMIN -> "Organization Root Admin"
                                            TenantRole.SUPERVISOR -> "Supervisor (HITL Approver)"
                                            TenantRole.OPERATOR -> "Agent Operator (Analyst)"
                                            TenantRole.AUDITOR -> "Security & Compliance Auditor"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (role) {
                                            TenantRole.ORG_ADMIN -> "Full access to destructive & side-effect tools"
                                            TenantRole.SUPERVISOR -> "Can approve/reject HITL gates & outbound outreach"
                                            TenantRole.OPERATOR -> "Can launch prospect pipelines & research"
                                            TenantRole.AUDITOR -> "Read-only access to cryptographic WORM ledger"
                                        },
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
