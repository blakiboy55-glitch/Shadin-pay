package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.User
import com.example.data.repository.BankingRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

@Composable
fun KycScreen(
    user: User,
    repository: BankingRepository,
    language: AppLanguage,
    onKycSubmitted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var nidNumber by remember { mutableStateOf(user.nidNumber.ifEmpty { "5918237461" }) }
    var dob by remember { mutableStateOf("15/08/1996") }
    var fathersName by remember { mutableStateOf("Md. Rafiqul Islam") }
    var isSubmitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("kyc_screen")
    ) {
        // Hero Art
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.kyc_security_art_1791562642424),
                contentDescription = "KYC Verification Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // KYC status card
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
                        text = Localization.tr("kyc_verification", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (user.kycStatus) {
                            "verified" -> StatusSuccess.copy(alpha = 0.15f)
                            "pending" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = when (user.kycStatus) {
                                "verified" -> if (language == AppLanguage.BANGLA) "যাচাইকৃত (Verified)" else "Verified"
                                "pending" -> if (language == AppLanguage.BANGLA) "যাচাইাধীন (In Review)" else "Pending Review"
                                else -> if (language == AppLanguage.BANGLA) "অযাচাইকৃত (Unverified)" else "Unverified"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (user.kycStatus) {
                                "verified" -> StatusSuccess
                                "pending" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "বাংলাদেশ ব্যাংকের ফাইন্যান্সিয়াল ইন্টেলিজেন্স ইউনিট (BFIU) নির্দেশিকা অনুযায়ী জাতীয় পরিচয়পত্র দিয়ে অ্যাকাউন্ট নিশ্চিত করুন।"
                    else "As per Bangladesh Bank BFIU regulatory compliance guidelines, verified KYC unlocks full transactional limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nidNumber,
                    onValueChange = { nidNumber = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "জাতীয় পরিচয়পত্র নম্বর (১০ বা ১৭ ডিজিট)" else "National ID Number (NID)") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = "NID", tint = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("nid_number_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "জন্ম তারিখ (দিন/মাস/বছর)" else "Date of Birth (DD/MM/YYYY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dob_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fathersName,
                    onValueChange = { fathersName = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "পিতার নাম" else "Father's Legal Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("fathers_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Verification checks
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KycCheckRow(if (language == AppLanguage.BANGLA) "জাতীয় তথ্যভান্ডার (EC) সাথে নাম ও ছবি মিলানো" else "EC National database biometric matching")
                    KycCheckRow(if (language == AppLanguage.BANGLA) "দৈনিক ৫০,০০০ টাকা লেনদেন সীমা উন্মুক্ত" else "Unlocks ৳50,000 daily transaction limit")
                    KycCheckRow(if (language == AppLanguage.BANGLA) "আন্তর্জাতিক রেমিট্যান্স প্রাপ্তির সুবিধা" else "Enables inbound foreign remittance")
                }
            }
        }

        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message!!, color = EmeraldPrimary, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (nidNumber.length < 10) {
                    message = if (language == AppLanguage.BANGLA) "সঠিক ১০ বা ১৭ ডিজিটের এনআইডি দিন" else "Enter a valid 10 or 17-digit NID"
                    return@Button
                }
                isSubmitting = true
                coroutineScope.launch {
                    try {
                        repository.updateUserProfile(
                            userId = user.userId,
                            updates = mapOf(
                                "nidNumber" to nidNumber,
                                "kycStatus" to "verified" // Auto-verify in simulation sandbox
                            )
                        )
                        isSubmitting = false
                        message = if (language == AppLanguage.BANGLA) "আপনার এনআইডি সফলভাবে যাচাই হয়েছে!" else "Your NID has been verified successfully!"
                        onKycSubmitted()
                    } catch (e: Exception) {
                        isSubmitting = false
                        message = e.localizedMessage ?: "Failed to submit"
                    }
                }
            },
            enabled = !isSubmitting,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_kyc_btn")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Verified, contentDescription = "Verify", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA) "এনআইডি যাচাই সম্পন্ন করুন" else "Verify Identity Now",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun KycCheckRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Check",
            tint = StatusSuccess,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
