package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
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
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

@Composable
fun MerchantPaymentScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var merchantCode by remember { mutableStateOf("01700998877") }
    var merchantName by remember { mutableStateOf("Aarong Flagship Outlet") }
    var counterRef by remember { mutableStateOf("INV-8921") }
    var amountInput by remember { mutableStateOf("1500") }

    var isPinDialogOpen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("merchant_payment_screen")
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("merchant_pay", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "দোকান, শপিংমল ও অনলাইন মার্চেন্টে দ্রুত পেমেন্ট করুন"
                    else "Instant checkout at retail merchants, superstores, and online services in Bangladesh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = merchantCode,
                    onValueChange = { merchantCode = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "মার্চেন্ট নম্বর / কিউআর কোড" else "Merchant Number / Code") },
                    leadingIcon = {
                        Icon(Icons.Default.Storefront, contentDescription = "Store", tint = EmeraldPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            merchantCode = "01700998877"
                            merchantName = "Shwapno Superstore (Gulshan)"
                        }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = EmeraldPrimary)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("merchant_code_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "মার্চেন্ট আউটলেটের নাম" else "Merchant Outlet Name") },
                    trailingIcon = {
                        Icon(Icons.Default.Verified, contentDescription = "Verified Merchant", tint = StatusSuccess)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("merchant_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = counterRef,
                    onValueChange = { counterRef = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "কাউন্টার / চালান নম্বর" else "Counter / Invoice Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("merchant_ref_input")
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
                    modifier = Modifier.fillMaxWidth().testTag("merchant_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "মার্চেন্ট পেমেন্ট ফি" else "Payment Fee",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (language == AppLanguage.BANGLA) "ফ্রি (৳০.০০)" else "Free (৳0.00)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusSuccess)
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
                if (merchantCode.length < 5) {
                    errorMessage = if (language == AppLanguage.BANGLA) "সঠিক মার্চেন্ট কোড দিন" else "Enter a valid merchant code"
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
                .testTag("merchant_proceed_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Storefront, contentDescription = "Pay", modifier = Modifier.size(18.dp))
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
                    recipientName = merchantName,
                    recipientPhoneOrId = merchantCode,
                    type = "MERCHANT_PAYMENT",
                    amount = amount,
                    fee = 0.0,
                    pin = pin,
                    reference = counterRef,
                    metadata = "Merchant: $merchantCode"
                )
                isProcessing = false
                result.fold(
                    onSuccess = { tx -> onSuccess(tx) },
                    onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Payment failed" }
                )
            }
        }
    )
}
