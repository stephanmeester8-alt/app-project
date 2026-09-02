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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.local.AuditRecordEntity
import com.example.data.model.PolicyVerdict
import com.example.ui.components.HashChip
import com.example.ui.components.PolicyVerdictBadge
import com.example.ui.components.WORMIntegrityBanner
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultGold
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.viewmodel.AIVaultsViewModel

@Composable
fun AuditLedgerScreen(
    viewModel: AIVaultsViewModel,
    modifier: Modifier = Modifier
) {
    val auditRecords by viewModel.auditRecords.collectAsStateWithLifecycle()
    val isChainValid by viewModel.isChainValid.collectAsStateWithLifecycle()
    val isVerifyingChain by viewModel.isVerifyingChain.collectAsStateWithLifecycle()

    var showRawJson by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // WORM Integrity Banner & Actions
            WORMIntegrityBanner(
                isValid = isChainValid,
                isVerifying = isVerifyingChain,
                onVerifyClick = { viewModel.verifyChainIntegrity() },
                onTamperClick = { viewModel.simulateTamperAttack() },
                onRestoreClick = { viewModel.restoreChain() }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Immutable WORM Ledger (${auditRecords.size} Blocks)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cryptographically chained SHA-256 evidence blocks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VaultNavyCardLight)
                        .border(1.dp, VaultBorder, RoundedCornerShape(8.dp))
                        .clickable { showRawJson = !showRawJson }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("toggle_siem_json")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showRawJson) "Cards" else "SIEM JSON", fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (showRawJson) {
            item {
                val jsonPreview = auditRecords.joinToString(",\n") { log ->
                    """  { "id": "${log.logId}", "tenant": "${log.tenantId}", "actor": "${log.actorId}", "tool": "${log.toolName}", "verdict": "${log.policyVerdict}", "currentHash": "${log.currentHash.take(16)}...", "prevHash": "${log.previousLogHash.take(16)}..." }"""
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VaultNavyCardLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SIEM Forwarder Event Stream (JSON Array):",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[\n$jsonPreview\n]",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        itemsIndexed(auditRecords) { index, log ->
            AuditBlockCard(
                log = log,
                blockNumber = index + 1,
                isLast = index == auditRecords.lastIndex
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AuditBlockCard(
    log: AuditRecordEntity,
    blockNumber: Int,
    isLast: Boolean
) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audit_block_${log.logId}"),
            colors = CardDefaults.cardColors(
                containerColor = if (log.isTampered) CoralAlert.copy(alpha = 0.12f) else VaultNavyCard
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (log.isTampered) CoralAlert else VaultBorder,
                        if (log.isTampered) CoralAlert.copy(alpha = 0.5f) else VaultBorder
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BLOCK #$blockNumber",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = log.toolName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    PolicyVerdictBadge(
                        verdict = runCatching { PolicyVerdict.valueOf(log.policyVerdict) }.getOrDefault(PolicyVerdict.PASS)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = log.details,
                    fontSize = 11.sp,
                    color = if (log.isTampered) CoralAlert else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cryptographic Hashes Chained Block View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(VaultNavyCardLight)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PREV HASH: ",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = log.previousLogHash.take(24) + "...",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CURR HASH: ",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (log.isTampered) CoralAlert else CyberCyan
                        )
                        Text(
                            text = log.currentHash.take(24) + "...",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (log.isTampered) CoralAlert else CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actor: ${log.actorId} | Tenant: ${log.tenantId}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = log.timestamp.takeLast(12).take(8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = CyberCyan.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
