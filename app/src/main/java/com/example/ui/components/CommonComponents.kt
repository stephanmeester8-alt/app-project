package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalStatus
import com.example.data.model.PolicyVerdict
import com.example.data.model.RiskLevel
import com.example.data.model.TenantRole
import com.example.data.model.WorkflowState
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultGold
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.theme.WarningAmber

@Composable
fun RiskBadge(risk: RiskLevel, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (risk) {
        RiskLevel.LOW -> Pair(EmeraldSafe.copy(alpha = 0.15f), EmeraldSafe)
        RiskLevel.MEDIUM -> Pair(WarningAmber.copy(alpha = 0.15f), WarningAmber)
        RiskLevel.HIGH -> Pair(VaultGold.copy(alpha = 0.2f), VaultGold)
        RiskLevel.CRITICAL -> Pair(CoralAlert.copy(alpha = 0.2f), CoralAlert)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("risk_badge_${risk.name}")
    ) {
        Text(
            text = risk.name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun RoleBadge(role: TenantRole, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (role) {
        TenantRole.ORG_ADMIN -> Pair(CoralAlert.copy(alpha = 0.15f), CoralAlert)
        TenantRole.SUPERVISOR -> Pair(CyberCyan.copy(alpha = 0.15f), CyberCyan)
        TenantRole.OPERATOR -> Pair(EmeraldSafe.copy(alpha = 0.15f), EmeraldSafe)
        TenantRole.AUDITOR -> Pair(PurpleAccent.copy(alpha = 0.15f), PurpleAccent)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("role_badge_${role.name}")
    ) {
        Text(
            text = role.name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun WorkflowStateBadge(state: WorkflowState, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (state) {
        WorkflowState.INITIALIZED -> Pair(Color.Gray.copy(alpha = 0.2f), Color.LightGray)
        WorkflowState.RESEARCHING,
        WorkflowState.AI_DETECTION,
        WorkflowState.QUALIFYING,
        WorkflowState.EXECUTING -> Pair(CyberCyan.copy(alpha = 0.15f), CyberCyan)
        WorkflowState.WAITING_FOR_HITL -> Pair(VaultGold.copy(alpha = 0.2f), VaultGold)
        WorkflowState.COMPLETED -> Pair(EmeraldSafe.copy(alpha = 0.15f), EmeraldSafe)
        WorkflowState.FAILED,
        WorkflowState.BLOCKED_BY_POLICY -> Pair(CoralAlert.copy(alpha = 0.2f), CoralAlert)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("workflow_state_${state.name}")
    ) {
        Text(
            text = state.name.replace("_", " "),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PolicyVerdictBadge(verdict: PolicyVerdict, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (verdict) {
        PolicyVerdict.PASS -> Pair(EmeraldSafe.copy(alpha = 0.15f), EmeraldSafe)
        PolicyVerdict.HALTED_FOR_HITL -> Pair(VaultGold.copy(alpha = 0.2f), VaultGold)
        PolicyVerdict.BLOCKED_BY_POLICY,
        PolicyVerdict.EXECUTION_FAILED -> Pair(CoralAlert.copy(alpha = 0.2f), CoralAlert)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("policy_verdict_${verdict.name}")
    ) {
        Text(
            text = verdict.name.replace("_", " "),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ApprovalStatusBadge(status: ApprovalStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        ApprovalStatus.PENDING -> Pair(VaultGold.copy(alpha = 0.2f), VaultGold)
        ApprovalStatus.APPROVED -> Pair(EmeraldSafe.copy(alpha = 0.15f), EmeraldSafe)
        ApprovalStatus.REJECTED,
        ApprovalStatus.EXPIRED -> Pair(CoralAlert.copy(alpha = 0.2f), CoralAlert)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("approval_status_${status.name}")
    ) {
        Text(
            text = status.name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HashChip(
    label: String,
    hash: String,
    modifier: Modifier = Modifier,
    isTampered: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val borderColor = if (isTampered) CoralAlert else VaultBorder
    val textColor = if (isTampered) CoralAlert else CyberCyan.copy(alpha = 0.85f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(VaultNavyCard)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = if (hash.length > 16) "${hash.take(8)}...${hash.takeLast(6)}" else hash,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("metric_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = VaultNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VaultBorder, accentColor.copy(alpha = 0.3f)))),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = accentColor
            )
        }
    }
}

@Composable
fun WORMIntegrityBanner(
    isValid: Boolean?,
    isVerifying: Boolean,
    onVerifyClick: () -> Unit,
    onTamperClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (bgColor, borderColor, statusColor, titleText, descText) = when {
        isVerifying -> listOf(
            VaultNavyCardLight,
            CyberCyan.copy(alpha = 0.5f),
            CyberCyan,
            "Scanning SHA-256 Hash Chain...",
            "Validating cryptographic WORM block seals..."
        )
        isValid == true -> listOf(
            EmeraldSafe.copy(alpha = 0.08f),
            EmeraldSafe.copy(alpha = 0.35f),
            EmeraldSafe,
            "WORM Ledger: 100% Cryptographically Intact",
            "SHA-256 block chain sealed & tamper-proof"
        )
        else -> listOf(
            CoralAlert.copy(alpha = 0.12f),
            CoralAlert.copy(alpha = 0.6f),
            CoralAlert,
            "⚠️ CRYPTOGRAPHIC TAMPER DETECTED",
            "Hash seal broken: Corrupted block identified in ledger"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("worm_integrity_banner"),
        colors = CardDefaults.cardColors(containerColor = bgColor as Color),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(borderColor as Color, (borderColor as Color).copy(alpha = 0.1f)))),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor as Color)
                            .alpha(if (isVerifying || isValid != true) pulseAlpha else 1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = titleText as String,
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = descText as String,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable(enabled = !isVerifying) { onVerifyClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("verify_chain_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify Chain", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isValid == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CoralAlert.copy(alpha = 0.12f))
                            .border(1.dp, CoralAlert.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable { onTamperClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("simulate_tamper_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CoralAlert, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Tamper", color = CoralAlert, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSafe.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldSafe.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onRestoreClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("restore_chain_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSafe, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore & Reseal", color = EmeraldSafe, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
