package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
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
import com.example.ui.components.*
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun CashOutScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var agentNumber by remember { mutableStateOf("01819876543") }
    var agentName by remember { mutableStateOf("Bismillah Telecom & Agent Point") }
    var amountInput by remember { mutableStateOf("1000") }

    var isPinDialogOpen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    // Standard Bangladesh MFS cash out fee: 1.49% (rounded)
    val fee = if (amount > 0) Math.round((amount * 0.0149) * 100.0) / 100.0 else 0.0
    val totalAmount = amount + fee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("cash_out_screen")
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("cash_out", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "অনুমোদিত এজেন্ট পয়েন্ট থেকে নগদ টাকা উত্তোলন করুন"
                    else "Withdraw cash at authorized ShadhinPay agent counters across Bangladesh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = agentNumber,
                    onValueChange = { agentNumber = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "এজেন্ট নম্বর" else "Agent Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Store, contentDescription = "Agent", tint = EmeraldPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { agentNumber = "01819876543" }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Agent QR", tint = EmeraldPrimary)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("agent_number_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "এজেন্ট পয়েন্টের নাম" else "Agent Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("agent_name_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Amount Entry Card
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
                    modifier = Modifier.fillMaxWidth().testTag("cash_out_amount_input")
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

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "ক্যাশ আউট ফি (১.৪৯%)" else "Cash Out Fee (1.49%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(Localization.formatTaka(fee, language), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                Localization.tr("total", language),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                Localization.formatTaka(totalAmount, language),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            )
                        }
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
                if (agentNumber.length < 8) {
                    errorMessage = if (language == AppLanguage.BANGLA) "সঠিক এজেন্ট নম্বর দিন" else "Enter a valid agent number"
                    return@Button
                }
                if (amount <= 0) {
                    errorMessage = if (language == AppLanguage.BANGLA) "টাকার পরিমাণ ০ এর বেশি হতে হবে" else "Amount must be greater than 0"
                    return@Button
                }
                val currentBal = wallet?.balance ?: 0.0
                if (currentBal < totalAmount) {
                    errorMessage = if (language == AppLanguage.BANGLA) "অপর্যাপ্ত ব্যালেন্স" else "Insufficient wallet balance"
                    return@Button
                }
                errorMessage = null
                isPinDialogOpen = true
            },
            enabled = !isProcessing,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("cash_out_proceed_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Payments, contentDescription = "Cash Out", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.tr("confirm_transfer", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    PinEntryDialog(
        isOpen = isPinDialogOpen,
        onDismiss = { isPinDialogOpen = false },
        language = language,
        onPinConfirmed = { pin ->
            isProcessing = true
            coroutineScope.launch {
                val result = repository.executeTransfer(
                    senderUser = user,
                    recipientName = agentName,
                    recipientPhoneOrId = agentNumber,
                    type = "CASH_OUT",
                    amount = amount,
                    fee = fee,
                    pin = pin,
                    reference = "Agent Cash Withdrawal",
                    metadata = "Agent: $agentNumber"
                )
                isProcessing = false
                result.fold(
                    onSuccess = { tx -> onSuccess(tx) },
                    onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Cash out failed" }
                )
            }
        }
    )
}
