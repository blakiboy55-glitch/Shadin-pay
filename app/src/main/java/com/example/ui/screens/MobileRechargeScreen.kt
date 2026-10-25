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
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialCatalog
import com.example.data.model.RechargeOffer
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.*
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun MobileRechargeScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onSuccess: (Transaction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedOperatorId by remember { mutableStateOf("GP") }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber.ifEmpty { "01712345678" }) }
    var isPrepaid by remember { mutableStateOf(true) }
    var amountInput by remember { mutableStateOf("50") }

    var isPinDialogOpen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedOperator = FinancialCatalog.operators.find { it.id == selectedOperatorId }
        ?: FinancialCatalog.operators.first()
    val amount = amountInput.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("recharge_screen")
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.tr("recharge", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "সকল বাংলাদেশি অপারেটরে দ্রুত রিচার্জ ও আকর্ষণীয় অফার"
                    else "Instant mobile top-up and exclusive packages for all Bangladeshi telecom operators",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Operator selector
                Text(
                    text = if (language == AppLanguage.BANGLA) "অপারেটর নির্বাচন করুন" else "Select Operator",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(FinancialCatalog.operators) { op ->
                        val isSelected = op.id == selectedOperatorId
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(op.colorHex).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                            modifier = Modifier
                                .clickable { selectedOperatorId = op.id }
                                .testTag("operator_${op.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SimCard,
                                    contentDescription = op.name,
                                    tint = Color(op.colorHex),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = op.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(op.colorHex) else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prepaid/Postpaid toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = isPrepaid,
                        onClick = { isPrepaid = true },
                        label = { Text(if (language == AppLanguage.BANGLA) "প্রিপেইড" else "Prepaid") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isPrepaid,
                        onClick = { isPrepaid = false },
                        label = { Text(if (language == AppLanguage.BANGLA) "পোস্টপেইড" else "Postpaid") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর" else "Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recharge_phone_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Offer Bundles
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "সেরা অফার ও বান্ডেল" else "Special Packages & Offers",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                selectedOperator.bundles.forEach { bundle ->
                    val isSelected = amountInput == bundle.amount.toInt().toString()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { amountInput = bundle.amount.toInt().toString() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) bundle.titleBn else bundle.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = (if (language == AppLanguage.BANGLA) bundle.descriptionBn else bundle.description) + " • " + (if (language == AppLanguage.BANGLA) bundle.validityBn else bundle.validity),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "৳${bundle.amount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountInput = it },
                    label = { Text(Localization.tr("enter_amount", language)) },
                    leadingIcon = {
                        Text("৳", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recharge_amount_input")
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
                if (phoneNumber.length < 8) {
                    errorMessage = if (language == AppLanguage.BANGLA) "সঠিক ফোন নম্বর দিন" else "Enter a valid phone number"
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
                .testTag("recharge_proceed_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.PhoneAndroid, contentDescription = "Recharge", modifier = Modifier.size(18.dp))
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
                    recipientName = "${selectedOperator.name} Recharge ($phoneNumber)",
                    recipientPhoneOrId = phoneNumber,
                    type = "MOBILE_RECHARGE",
                    amount = amount,
                    fee = 0.0,
                    pin = pin,
                    reference = if (isPrepaid) "Prepaid Top-Up" else "Postpaid Bill",
                    metadata = "Operator: ${selectedOperator.name}"
                )
                isProcessing = false
                result.fold(
                    onSuccess = { tx -> onSuccess(tx) },
                    onFailure = { ex -> errorMessage = ex.localizedMessage ?: "Recharge failed" }
                )
            }
        }
    )
}
