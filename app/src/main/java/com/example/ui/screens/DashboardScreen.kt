package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Beneficiary
import com.example.data.model.FinancialCatalog
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    user: User,
    wallet: Wallet?,
    recentTransactions: List<Transaction>,
    beneficiaries: List<Beneficiary>,
    language: AppLanguage,
    onNavigateTo: (String) -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    onSelectBeneficiary: (Beneficiary) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // User Greeting & Balance Card
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "স্বাগতম," else "Hello,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = user.fullName.ifEmpty { "ShadhinPay User" },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        // KYC status pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (user.kycStatus) {
                                "verified" -> StatusSuccess.copy(alpha = 0.3f)
                                "pending" -> AmberAccent.copy(alpha = 0.3f)
                                else -> Color.White.copy(alpha = 0.2f)
                            },
                            modifier = Modifier
                                .clickable { onNavigateTo("kyc") }
                                .testTag("dashboard_kyc_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (user.kycStatus == "verified") Icons.Default.Verified
                                    else Icons.Default.Shield,
                                    contentDescription = "KYC Status",
                                    tint = if (user.kycStatus == "verified") Color(0xFF66FFA6)
                                    else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (user.kycStatus) {
                                        "verified" -> if (language == AppLanguage.BANGLA) "যাচাইকৃত" else "Verified"
                                        "pending" -> if (language == AppLanguage.BANGLA) "যাচাই চলছে" else "In Review"
                                        else -> if (language == AppLanguage.BANGLA) "এনআইডি দিন" else "Verify KYC"
                                    },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BalancePillCard(
                        balance = wallet?.balance ?: 0.0,
                        language = language
                    )
                }
            }
        }

        // Quick Service Grid (8 Core Services)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Send,
                            label = Localization.tr("send_money", language),
                            onClick = { onNavigateTo("send_money") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.AddCard,
                            label = Localization.tr("add_money", language),
                            onClick = { onNavigateTo("add_money") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Payments,
                            label = Localization.tr("cash_out", language),
                            onClick = { onNavigateTo("cash_out") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PhoneAndroid,
                            label = Localization.tr("recharge", language),
                            onClick = { onNavigateTo("recharge") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Storefront,
                            label = Localization.tr("merchant_pay", language),
                            onClick = { onNavigateTo("merchant") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.ReceiptLong,
                            label = Localization.tr("pay_bill", language),
                            onClick = { onNavigateTo("bill_pay") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.AccountBalance,
                            label = Localization.tr("bank_transfer", language),
                            onClick = { onNavigateTo("bank_transfer") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.QrCodeScanner,
                            label = Localization.tr("qr_pay", language),
                            onClick = { onNavigateTo("qr_pay") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Promotional Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateTo("send_money") }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_promo_1791562615878),
                    contentDescription = "Special Cash Reward Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        }

        // Favorite Recipients
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.tr("favorite_contacts", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val allBeneficiaries = if (beneficiaries.isNotEmpty()) beneficiaries
                else FinancialCatalog.sampleRecipients

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(allBeneficiaries) { item ->
                        FavoriteRecipientItem(item = item, onClick = { onSelectBeneficiary(item) })
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Column(modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.tr("recent_activity", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = { onNavigateTo("transactions") },
                        modifier = Modifier.testTag("view_all_transactions_btn")
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "সব দেখুন" else "View All",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View All",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (recentTransactions.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "No transactions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == AppLanguage.BANGLA)
                                        "এখনো কোনো লেনদেন হয়নি"
                                    else "No transactions recorded yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    recentTransactions.take(5).forEach { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            language = language,
                            onClick = { onSelectTransaction(tx) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteRecipientItem(
    item: Beneficiary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
            .testTag("fav_contact_${item.name.replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.firstOrNull()?.uppercase() ?: "P",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: Transaction,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val isCredit = transaction.type == "ADD_MONEY"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_card_${transaction.transactionId}")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCredit) StatusSuccess.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            "ADD_MONEY" -> Icons.Default.Add
                            "SEND_MONEY" -> Icons.Default.Send
                            "CASH_OUT" -> Icons.Default.Payments
                            "MOBILE_RECHARGE" -> Icons.Default.PhoneAndroid
                            "MERCHANT_PAYMENT" -> Icons.Default.Storefront
                            "BILL_PAY" -> Icons.Default.ReceiptLong
                            "BANK_TRANSFER" -> Icons.Default.AccountBalance
                            else -> Icons.Default.Payment
                        },
                        contentDescription = transaction.type,
                        tint = if (isCredit) StatusSuccess else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.recipientName.ifEmpty { transaction.type },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = transaction.type + if (transaction.reference.isNotEmpty()) " • ${transaction.reference}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isCredit) "+" else "-") + Localization.formatTaka(transaction.amount, language),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) StatusSuccess else MaterialTheme.colorScheme.onSurface
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (transaction.status) {
                        "SUCCESS" -> StatusSuccess.copy(alpha = 0.12f)
                        "REVERSED" -> StatusReversed.copy(alpha = 0.12f)
                        "FAILED" -> StatusFailed.copy(alpha = 0.12f)
                        else -> AmberAccent.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = transaction.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = when (transaction.status) {
                            "SUCCESS" -> StatusSuccess
                            "REVERSED" -> StatusReversed
                            "FAILED" -> StatusFailed
                            else -> AmberAccent
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
