package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RoleBadge
import com.example.ui.screens.ApprovalsScreen
import com.example.ui.screens.AuditLedgerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PolicySimulatorScreen
import com.example.ui.screens.TenantSwitcherSheet
import com.example.ui.screens.WorkflowsScreen
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultGold
import com.example.ui.theme.VaultNavyCard
import com.example.ui.theme.VaultNavyCardLight
import com.example.ui.viewmodel.AIVaultsViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Hub", Icons.Default.Dashboard),
    WORKFLOWS("Workflows", Icons.Default.AccountTree),
    APPROVALS("HITL Gates", Icons.Default.PendingActions),
    LEDGER("WORM Chain", Icons.Default.Lock),
    POLICY_SIM("Policies", Icons.Default.Policy)
}

@Composable
fun MainScaffold(viewModel: AIVaultsViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showTenantSheet by remember { mutableStateOf(false) }

    val tenantContext by viewModel.tenantContext.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingApprovalsCount.collectAsStateWithLifecycle()
    val isChainValid by viewModel.isChainValid.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    if (showTenantSheet) {
        TenantSwitcherSheet(
            viewModel = viewModel,
            onDismiss = { showTenantSheet = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VaultNavyCard)
                    .border(1.dp, VaultBorder)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showTenantSheet = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AIVaults AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "v2.5",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberCyan
                                )
                            }
                            Text(
                                text = "${tenantContext.tenantId.takeLast(10)} • ${tenantContext.userId}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoleBadge(role = tenantContext.role)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChainValid == true) EmeraldSafe.copy(alpha = 0.15f) else CoralAlert.copy(alpha = 0.2f))
                                .border(1.dp, if (isChainValid == true) EmeraldSafe.copy(alpha = 0.4f) else CoralAlert.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { selectedTab = NavigationTab.LEDGER }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("top_bar_worm_status")
                        ) {
                            Text(
                                text = if (isChainValid == true) "WORM: OK" else "WORM: ALERT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChainValid == true) EmeraldSafe else CoralAlert,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { showTenantSheet = true },
                            modifier = Modifier.size(28.dp).testTag("tenant_switcher_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Tenant Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = VaultNavyCard,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, VaultBorder)
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == NavigationTab.APPROVALS && pendingCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = VaultGold, contentColor = Color.Black) {
                                        Text("$pendingCount", fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToWorkflows = { selectedTab = NavigationTab.WORKFLOWS },
                        onNavigateToApprovals = { selectedTab = NavigationTab.APPROVALS },
                        onNavigateToLedger = { selectedTab = NavigationTab.LEDGER },
                        onNavigateToPolicySim = { selectedTab = NavigationTab.POLICY_SIM }
                    )
                    NavigationTab.WORKFLOWS -> WorkflowsScreen(
                        viewModel = viewModel,
                        onNavigateToApprovals = { selectedTab = NavigationTab.APPROVALS }
                    )
                    NavigationTab.APPROVALS -> ApprovalsScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.LEDGER -> AuditLedgerScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.POLICY_SIM -> PolicySimulatorScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
