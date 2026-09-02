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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.HITLApprovalEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.TenantRole
import com.example.ui.components.ApprovalStatusBadge
import com.example.ui.components.HashChip
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
fun ApprovalsScreen(
    viewModel: AIVaultsViewModel,
    modifier: Modifier = Modifier
) {
    val tenantContext by viewModel.tenantContext.collectAsStateWithLifecycle()
    val allApprovals by viewModel.allApprovals.collectAsStateWithLifecycle()

    var filterStatus by remember { mutableStateOf<ApprovalStatus?>(null) }

    val filteredList = if (filterStatus != null) {
        allApprovals.filter { it.status == filterStatus }
    } else {
        allApprovals
    }

    val pendingCount = allApprovals.count { it.status == ApprovalStatus.PENDING }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Security Gate Info Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hitl_info_banner"),
                colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, VaultGold.copy(alpha = 0.35f)))),
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
                                text = "Human-In-The-Loop (HITL) Gate",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "High-risk external side-effects & destructive mutations require explicit supervisor authorization.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VaultGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = VaultGold, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Your Active Identity: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            RoleBadge(role = tenantContext.role)
                        }

                        if (tenantContext.role != TenantRole.SUPERVISOR && tenantContext.role != TenantRole.ORG_ADMIN) {
                            Text(
                                text = "Read-Only (Switch to Supervisor)",
                                fontSize = 10.sp,
                                color = CoralAlert,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "All (${allApprovals.size})",
                    selected = filterStatus == null,
                    onClick = { filterStatus = null }
                )
                FilterChipItem(
                    label = "Pending ($pendingCount)",
                    selected = filterStatus == ApprovalStatus.PENDING,
                    onClick = { filterStatus = ApprovalStatus.PENDING }
                )
                FilterChipItem(
                    label = "Approved",
                    selected = filterStatus == ApprovalStatus.APPROVED,
                    onClick = { filterStatus = ApprovalStatus.APPROVED }
                )
                FilterChipItem(
                    label = "Rejected",
                    selected = filterStatus == ApprovalStatus.REJECTED,
                    onClick = { filterStatus = ApprovalStatus.REJECTED }
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No approval requests matching filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        items(filteredList) { approval ->
            ApprovalItemCard(
                approval = approval,
                currentRole = tenantContext.role,
                onApprove = { viewModel.resolveApproval(approval.approvalId, ApprovalStatus.APPROVED) },
                onReject = { viewModel.resolveApproval(approval.approvalId, ApprovalStatus.REJECTED) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) CyberCyan.copy(alpha = 0.2f) else VaultNavyCard)
            .border(1.dp, if (selected) CyberCyan else VaultBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("filter_chip_$label")
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ApprovalItemCard(
    approval: HITLApprovalEntity,
    currentRole: TenantRole,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val canResolve = currentRole == TenantRole.ORG_ADMIN || currentRole == TenantRole.SUPERVISOR

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("approval_card_${approval.approvalId}"),
        colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    VaultBorder,
                    if (approval.status == ApprovalStatus.PENDING) VaultGold.copy(alpha = 0.3f) else VaultBorder
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskBadge(risk = approval.riskLevel)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = approval.toolName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ApprovalStatusBadge(status = approval.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = approval.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target: ${approval.target}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberCyan
                )
                Text(
                    text = "Tenant: ${approval.tenantId}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // HMAC Signature Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(VaultNavyCardLight)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = VaultGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("HMAC-SHA256: ", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${approval.hmacSignature.take(12)}...${approval.hmacSignature.takeLast(6)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = VaultGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Signed",
                    fontSize = 9.sp,
                    color = EmeraldSafe,
                    fontWeight = FontWeight.Bold
                )
            }

            if (approval.status == ApprovalStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReject,
                        enabled = canResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAlert.copy(alpha = 0.2f), contentColor = CoralAlert),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CoralAlert.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .testTag("reject_button_${approval.approvalId}")
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject & Halt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        enabled = canResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSafe, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_button_${approval.approvalId}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resolved by ${approval.resolvedBy ?: "System"} at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(approval.resolvedAt ?: approval.createdAt)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
