package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadhinTopBar(
    title: String,
    language: AppLanguage,
    onLanguageToggle: () -> Unit,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    isAdmin: Boolean = false,
    onAdminClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("app_bar_title")
                )
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AmberAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "৳",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        },
        actions = {
            // Language Switcher Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLanguageToggle() }
                    .testTag("language_toggle_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "বাং" else "EN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (isAdmin) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.testTag("admin_portal_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Dashboard",
                        tint = AmberAccentNight
                    )
                }
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.testTag("notification_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(containerColor = AmberAccent) {
                                Text(unreadNotificationCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun BalancePillCard(
    balance: Double,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var isRevealed by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { isRevealed = !isRevealed }
            .testTag("balance_pill_card")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Balance",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = Localization.tr("available_balance", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedContent(
                        targetState = isRevealed,
                        label = "balance_anim"
                    ) { revealed ->
                        if (revealed) {
                            Text(
                                text = Localization.formatTaka(balance, language),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        } else {
                            Text(
                                text = "৳ • • • • • •",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isRevealed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (isRevealed) (if (language == AppLanguage.BANGLA) "লুকান" else "Hide")
                    else (if (language == AppLanguage.BANGLA) "ট্যাপ করুন" else "Tap"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isRevealed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = EmeraldPrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag("action_${label.replace(" ", "_").lowercase()}")
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun PinEntryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    language: AppLanguage,
    onPinConfirmed: (String) -> Unit
) {
    if (!isOpen) return

    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.tr("enter_pin", language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "লেনদেন সম্পন্ন করতে আপনার ৪ ডিজিটের গোপন পিন দিন"
                    else "Enter your 4-digit secret PIN to authorize this transaction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            errorMessage = ""
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    placeholder = { Text("• • • •") },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .testTag("pin_input_field")
                )
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length == 4) {
                        onPinConfirmed(pin)
                        onDismiss()
                    } else {
                        errorMessage = if (language == AppLanguage.BANGLA)
                            "সঠিক ৪ ডিজিটের পিন আবশ্যক" else "Must be 4 digits"
                    }
                },
                modifier = Modifier.testTag("pin_confirm_btn")
            ) {
                Text(if (language == AppLanguage.BANGLA) "যাচাই করুন" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("pin_cancel_btn")
            ) {
                Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionReceiptSheet(
    transaction: Transaction?,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    if (transaction == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(EmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = StatusSuccess,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (language == AppLanguage.BANGLA) "লেনদেন সফল হয়েছে" else "Transaction Successful",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ref: ${transaction.transactionId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ReceiptRow(
                        label = if (language == AppLanguage.BANGLA) "লেনদেনের ধরন" else "Type",
                        value = transaction.type
                    )
                    ReceiptRow(
                        label = if (language == AppLanguage.BANGLA) "প্রাপক" else "Recipient",
                        value = transaction.recipientName
                    )
                    if (transaction.recipientPhone.isNotEmpty()) {
                        ReceiptRow(
                            label = if (language == AppLanguage.BANGLA) "প্রাপক নম্বর/অ্যাকাউন্ট" else "Account/Number",
                            value = transaction.recipientPhone
                        )
                    }
                    ReceiptRow(
                        label = if (language == AppLanguage.BANGLA) "মূল টাকা" else "Amount",
                        value = Localization.formatTaka(transaction.amount, language)
                    )
                    ReceiptRow(
                        label = if (language == AppLanguage.BANGLA) "চার্জ / ফি" else "Fee",
                        value = Localization.formatTaka(transaction.fee, language)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ReceiptRow(
                        label = if (language == AppLanguage.BANGLA) "মোট কর্তন" else "Total Deducted",
                        value = Localization.formatTaka(transaction.totalAmount, language),
                        isBold = true
                    )
                    if (transaction.reference.isNotEmpty()) {
                        ReceiptRow(
                            label = if (language == AppLanguage.BANGLA) "রেফারেন্স" else "Reference",
                            value = transaction.reference
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_close_btn")
            ) {
                Text(if (language == AppLanguage.BANGLA) "সম্পন্ন" else "Done")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isBold) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
