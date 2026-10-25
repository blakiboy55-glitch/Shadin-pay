package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialCatalog
import com.example.data.model.ScheduledBank
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.*
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun BankTransferScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedBankCode by remember { mutableStateOf("BRAC") }
    var selectedMethod by remember { mutableStateOf("NPSB") }
    var accountNumber by remember { mutableStateOf("1501204892019001") }
    var accountHolderName by remember { mutableStateOf("Mahmudul Hasan") }
    var amountInput by remember { mutableStateOf("5000") }

    var isPinDialogOpen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedBank = FinancialCatalog.banks.find { it.code == selectedBankCode }
        ?: FinancialCatalog.banks.first()

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val fee = if (selectedMethod == "NPSB" && amount > 0) 10.0 else 0.0
    val totalAmount = amount + fee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("bank_transfer_screen")
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("bank_transfer", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "বাংলাদেশের তফসিলি ব্যাংকে NPSB ও BEFTN মাধ্যমে সরাসরি ফান্ড ট্রান্সফার"
                    else "Direct fund transfer to scheduled commercial banks in Bangladesh via NPSB/BEFTN",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.BANGLA) "ব্যাংক নির্বাচন করুন" else "Select Bank",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FinancialCatalog.banks.forEach { bank ->
                    val isSelected = bank.code == selectedBankCode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedBankCode = bank.code }
                            .testTag("bank_${bank.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = bank.name,
                                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA) bank.nameBn else bank.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Routing Method
                Text(
                    text = if (language == AppLanguage.BANGLA) "ট্রান্সফার চ্যানেল" else "Transfer Channel",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NPSB", "BEFTN").forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = {
                                Text(if (method == "NPSB") "NPSB (তাৎক্ষণিক / Instant - ৳১০)" else "BEFTN (পরবর্তী কার্যদিবস / ৳০)")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Account Details & Amount
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "ব্যাংক হিসাব নম্বর" else "Bank Account Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_account_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = accountHolderName,
                    onValueChange = { accountHolderName = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "হিসাবধারীর নাম" else "Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_holder_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountInput = it },
                    label = { Text(Localization.tr("enter_amount", language)) },
                    leadingIcon = {
                        Text("৳", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                            Text(Localization.tr("fee", language), style = MaterialTheme.typography.bodySmall)
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
                if (accountNumber.length < 8) {
                    errorMessage = if (language == AppLanguage.BANGLA) "সঠিক হিসাব নম্বর দিন" else "Enter a valid account number"
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
                .testTag("bank_proceed_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.AccountBalance, contentDescription = "Transfer", modifier = Modifier.size(18.dp))
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
                    recipientName = "$accountHolderName (${selectedBank.name})",
                    recipientPhoneOrId = accountNumber,
                    type = "BANK_TRANSFER",
                    amount = amount,
                    fee = fee,
                    pin = pin,
                    reference = "Channel: $selectedMethod",
                    metadata = "Bank: ${selectedBank.code}, Method: $selectedMethod"
                )
                isProcessing = false
                result.fold(
                    onSuccess = { tx -> onSuccess(tx) },
                    onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Bank transfer failed" }
                )
            }
        }
    )
}
