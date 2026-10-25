package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.repository.BankingRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    adminUser: User,
    repository: BankingRepository,
    language: AppLanguage,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(0) } // 0: Overview, 1: Users & KYC, 2: Transactions & Reversals

    var usersList by remember { mutableStateOf<List<User>>(emptyList()) }
    var transactionsList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isOperating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            repository.observeAllUsersForAdmin().collect { users ->
                usersList = users
            }
        }
        coroutineScope.launch {
            repository.observeAllTransactionsForAdmin().collect { txs ->
                transactionsList = txs
            }
        }
    }

    val totalVolume = remember(transactionsList) {
        transactionsList.filter { it.status == "SUCCESS" }.sumOf { it.amount }
    }
    val pendingKycCount = remember(usersList) {
        usersList.count { it.kycStatus == "pending" || it.kycStatus == "unverified" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.tr("admin_panel", language),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Admin Session: ${adminUser.fullName} (${adminUser.email})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "SUPER ADMIN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberAccentDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text(if (language == AppLanguage.BANGLA) "ওভারভিউ" else "Metrics") }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text(if (language == AppLanguage.BANGLA) "ব্যবহারকারী ও KYC" else "Users & KYC") }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text(if (language == AppLanguage.BANGLA) "লেনদেন ও রিভার্সাল" else "Ledger & Reversals") }
            )
        }

        if (actionMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
            ) {
                Text(
                    text = actionMessage!!,
                    color = OnEmeraldContainer,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSection) {
            0 -> {
                // Overview metrics
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = if (language == AppLanguage.BANGLA) "মোট লেনদেন ভলিউম" else "Total Volume",
                                value = Localization.formatTaka(totalVolume, language),
                                icon = Icons.Default.TrendingUp,
                                iconTint = EmeraldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = if (language == AppLanguage.BANGLA) "নিবন্ধিত অ্যাকাউন্ট" else "Total Users",
                                value = usersList.size.toString(),
                                icon = Icons.Default.People,
                                iconTint = AmberAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = if (language == AppLanguage.BANGLA) "লেনদেনের সংখ্যা" else "Transactions",
                                value = transactionsList.size.toString(),
                                icon = Icons.Default.Receipt,
                                iconTint = StatusSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = if (language == AppLanguage.BANGLA) "অপেক্ষমাণ KYC" else "Pending KYC",
                                value = pendingKycCount.toString(),
                                icon = Icons.Default.Shield,
                                iconTint = StatusFailed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "সিস্টেম অডিট ও নিরাপত্তা নীতি" else "Compliance & Security Enforcement",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Zero-trust security rules deployed on database\n• Append-only transaction ledger with immutable audit records\n• Anti-money laundering (AML) velocity limit checks enabled\n• Real-time atomic database transactions prevent double spending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Users & KYC Table
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(usersList) { u ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = u.fullName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "${u.phoneNumber} • NID: ${u.nidNumber.ifEmpty { "None" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (u.status == "active") StatusSuccess.copy(alpha = 0.15f) else StatusFailed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = u.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (u.status == "active") StatusSuccess else StatusFailed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "KYC: ${u.kycStatus.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (u.kycStatus != "verified") {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        isOperating = true
                                                        repository.adminUpdateKycStatus(u.userId, "verified")
                                                        isOperating = false
                                                        actionMessage = "Verified KYC for ${u.fullName}"
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Approve KYC", fontSize = 11.sp)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val nextStatus = if (u.status == "active") "restricted" else "active"
                                                coroutineScope.launch {
                                                    isOperating = true
                                                    repository.adminUpdateUserStatus(u.userId, nextStatus)
                                                    isOperating = false
                                                    actionMessage = "Status for ${u.fullName} changed to $nextStatus"
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(if (u.status == "active") "Restrict" else "Activate", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Transactions & Reversal Tool
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(transactionsList) { tx ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = tx.transactionId, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "${tx.type} • From: ${tx.senderName} ➜ To: ${tx.recipientName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = Localization.formatTaka(tx.amount, language),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (tx.status) {
                                            "SUCCESS" -> StatusSuccess.copy(alpha = 0.15f)
                                            "REVERSED" -> StatusReversed.copy(alpha = 0.15f)
                                            else -> StatusFailed.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = tx.status,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = when (tx.status) {
                                                "SUCCESS" -> StatusSuccess
                                                "REVERSED" -> StatusReversed
                                                else -> StatusFailed
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (tx.status == "SUCCESS") {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isOperating = true
                                                    val res = repository.adminReverseTransaction(tx.transactionId)
                                                    isOperating = false
                                                    res.fold(
                                                        onSuccess = { actionMessage = "Reversed transaction ${tx.transactionId} and refunded sender." },
                                                        onFailure = { actionMessage = "Reversal error: ${it.localizedMessage}" }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusReversed),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp).testTag("reverse_btn_${tx.transactionId}")
                                        ) {
                                            Text("Issue Reversal / Refund", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
        }
    }
}
