package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Beneficiary
import com.example.data.model.FinancialCatalog
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.*
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun SendMoneyScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    beneficiaries: List<Beneficiary>,
    prefilledRecipient: Beneficiary?,
    language: AppLanguage,
    onTransactionSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var recipientPhone by remember { mutableStateOf(prefilledRecipient?.phoneOrAccount ?: "01712345678") }
    var recipientName by remember { mutableStateOf(prefilledRecipient?.name ?: "Tanvir Hasan") }
    var amountInput by remember { mutableStateOf("500") }
    var referenceInput by remember { mutableStateOf("") }

    var isPinDialogOpen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val fee = if (amount > 0) 5.0 else 0.0
    val totalAmount = amount + fee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("send_money_screen")
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("send_money", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "যেকোনো ব্যক্তিগত শাদিনপে বা মোবাইল ওয়ালেটে নিমেষেই টাকা পাঠান"
                    else "Instant transfer to any ShadhinPay or registered mobile account in Bangladesh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Recipient Phone
                OutlinedTextField(
                    value = recipientPhone,
                    onValueChange = {
                        recipientPhone = it
                        // Auto-match recipient name from demo catalog
                        val match = FinancialCatalog.sampleRecipients.find { rec -> rec.phoneOrAccount.contains(it) }
                        if (match != null) recipientName = match.name
                    },
                    label = { Text(if (language == AppLanguage.BANGLA) "প্রাপকের মোবাইল নম্বর" else "Recipient Mobile Number") },
                    trailingIcon = {
                        Icon(Icons.Default.ContactPhone, contentDescription = "Contacts", tint = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recipient_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "প্রাপকের নাম" else "Recipient Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recipient_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Recipient Chips
                Text(
                    text = Localization.tr("favorite_contacts", language),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val allRecipients = if (beneficiaries.isNotEmpty()) beneficiaries else FinancialCatalog.sampleRecipients
                    items(allRecipients) { rec ->
                        SuggestionChip(
                            onClick = {
                                recipientPhone = rec.phoneOrAccount
                                recipientName = rec.name
                            },
                            label = { Text(rec.name) }
                        )
                    }
                }
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
                    modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Amount Chips
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = referenceInput,
                    onValueChange = { referenceInput = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "রেফারেন্স (ঐচ্ছিক)" else "Reference / Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("transfer_ref_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fee and Total Breakdown
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
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (recipientPhone.length < 8) {
                    errorMessage = if (language == AppLanguage.BANGLA) "সঠিক ফোন নম্বর দিন" else "Enter a valid phone number"
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
                .testTag("send_money_proceed_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
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
                    recipientName = recipientName,
                    recipientPhoneOrId = recipientPhone,
                    type = "SEND_MONEY",
                    amount = amount,
                    fee = fee,
                    pin = pin,
                    reference = referenceInput,
                    metadata = "Direct Send Money"
                )
                isProcessing = false
                result.fold(
                    onSuccess = { tx ->
                        onTransactionSuccess(tx)
                    },
                    onFailure = { ex ->
                        errorMessage = ex.localizedMessage ?: "Transaction failed"
                    }
                )
            }
        }
    )
}
