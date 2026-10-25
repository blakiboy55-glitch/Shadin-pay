package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.UtilityBiller
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.*
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun BillPaymentScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val categories = listOf("All", "Electricity", "Gas", "Water", "Internet", "Television", "Education")
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedBillerId by remember { mutableStateOf("DESCO") }
    var accountInput by remember { mutableStateOf("102938475") }
    var amountInput by remember { mutableStateOf("1250") }

    var isPinDialogOpen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filteredBillers = remember(selectedCategory) {
        if (selectedCategory == "All") FinancialCatalog.billers
        else FinancialCatalog.billers.filter { it.category == selectedCategory }
    }
    val selectedBiller = FinancialCatalog.billers.find { it.id == selectedBillerId }
        ?: FinancialCatalog.billers.first()

    val amount = amountInput.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("bill_payment_screen")
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("pay_bill", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "বিদ্যুৎ, গ্যাস, পানি, ইন্টারনেট ও শিক্ষা প্রতিষ্ঠানের সরকারি ফি পরিশোধ করুন"
                    else "Pay utility, electricity, gas, water, internet, and educational fees across Bangladesh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Categories
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == AppLanguage.BANGLA) "প্রতিষ্ঠান / বিলার" else "Select Biller Organization",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                filteredBillers.forEach { biller ->
                    val isSelected = biller.id == selectedBillerId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedBillerId = biller.id
                                accountInput = biller.exampleAccount
                            }
                            .testTag("biller_${biller.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) biller.nameBn else biller.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.BANGLA) biller.categoryBn else biller.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Account & Amount
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                OutlinedTextField(
                    value = accountInput,
                    onValueChange = { accountInput = it },
                    label = { Text(if (language == AppLanguage.BANGLA) selectedBiller.accountLabelBn else selectedBiller.accountLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("biller_account_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountInput = it },
                    label = { Text(Localization.tr("enter_amount", language)) },
                    leadingIcon = {
                        Text("৳", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("biller_amount_input")
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (accountInput.length < 5) {
                    errorMessage = if (language == AppLanguage.BANGLA) "সঠিক বিল হিসাব নম্বর দিন" else "Enter a valid bill account number"
                    return@Button
                }
                if (amount <= 0) {
                    errorMessage = if (language == AppLanguage.BANGLA) "টাকার পরিমাণ ০ এর বেশি হতে হবে" else "Amount must be greater than 0"
                    return@Button
                }
                val currentBal = wallet?.balance ?: 0.0
                if (currentBal < amount) {
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
                .testTag("biller_proceed_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.ReceiptLong, contentDescription = "Pay Bill", modifier = Modifier.size(18.dp))
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
                    recipientName = "${selectedBiller.name} ($accountInput)",
                    recipientPhoneOrId = accountInput,
                    type = "BILL_PAY",
                    amount = amount,
                    fee = 0.0,
                    pin = pin,
                    reference = "Utility Bill: $accountInput",
                    metadata = "Biller: ${selectedBiller.id}"
                )
                isProcessing = false
                result.fold(
                    onSuccess = { tx -> onSuccess(tx) },
                    onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Bill payment failed" }
                )
            }
        }
    )
}
