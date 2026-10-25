package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

@Composable
fun AddMoneyScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedChannel by remember { mutableStateOf("Bank Card (Visa/Mastercard)") }
    var amountInput by remember { mutableStateOf("1000") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0

    val channels = listOf(
        Triple("Bank Card (Visa/Mastercard)", "কার্ড (ভিসা/মাস্টারকার্ড)", Icons.Default.CreditCard),
        Triple("Internet Banking (City/BRAC/DBBL)", "ইন্টারনেট ব্যাংকিং", Icons.Default.AccountBalance),
        Triple("Authorized Agent Cash-In", "অনুমোদিত এজেন্ট ক্যাশ-ইন", Icons.Default.Storefront),
        Triple("MFS Linked Channel (bKash/Nagad)", "এমএফএস লিংকড চ্যানেল", Icons.Default.MobileFriendly)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("add_money_screen")
    ) {
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
                    Text(
                        text = Localization.tr("add_money", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Sandbox Gateway",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.BANGLA) "টাকা যোগ করার মাধ্যম নির্বাচন করুন" else "Select Funding Source",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                channels.forEach { (channelEn, channelBn, icon) ->
                    val isSelected = selectedChannel == channelEn
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedChannel = channelEn }
                            .testTag("channel_${channelEn.take(10)}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = channelEn,
                                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA) channelBn else channelEn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Amount card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("enter_amount", language),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountInput = it },
                    leadingIcon = {
                        Text("৳", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_money_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500, 1000, 2000, 5000).forEach { preset ->
                        FilterChip(
                            selected = amountInput == preset.toString(),
                            onClick = { amountInput = preset.toString() },
                            label = { Text("৳$preset") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (amount <= 0) {
                    errorMessage = if (language == AppLanguage.BANGLA) "টাকার পরিমাণ ০ এর বেশি হতে হবে" else "Amount must be greater than 0"
                    return@Button
                }
                isProcessing = true
                errorMessage = null
                coroutineScope.launch {
                    val result = repository.executeAddMoney(
                        user = user,
                        sourceMethod = selectedChannel,
                        amount = amount,
                        reference = "Sandbox Gateway Deposit"
                    )
                    isProcessing = false
                    result.fold(
                        onSuccess = { tx -> onSuccess(tx) },
                        onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Deposit failed" }
                    )
                }
            },
            enabled = !isProcessing,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("add_money_submit_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA) "টাকা যোগ করুন" else "Add Money to Wallet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
