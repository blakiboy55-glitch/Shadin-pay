package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportTicket
import com.example.data.model.User
import com.example.data.repository.BankingRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SupportScreen(
    user: User,
    repository: BankingRepository,
    language: AppLanguage
) {
    val coroutineScope = rememberCoroutineScope()
    var category by remember { mutableStateOf("Wrong Number Transfer") }
    var txIdInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccess by remember { mutableStateOf<String?>(null) }

    var userTickets by remember { mutableStateOf<List<SupportTicket>>(emptyList()) }

    LaunchedEffect(user.userId) {
        if (user.userId.isNotEmpty()) {
            repository.observeSupportTickets(user.userId).collect {
                userTickets = it
            }
        }
    }

    val categories = listOf(
        "Wrong Number Transfer",
        "Delayed Mobile Recharge",
        "Merchant Dispute",
        "Failed Bank Transfer Refund",
        "Suspected Fraud Alert",
        "General Inquiry"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("support_screen")
    ) {
        // Helpline banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = "Helpline", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "২৪/৭ গ্রাহক সেবা হটলাইন" else "24/7 Customer Care Helpline",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (language == AppLanguage.BANGLA) "কল করুন: ১৬২৪৭ অথবা +৮৮০ ৯৬১২১৬২৪৭" else "Dial: 16247 or +880 9612-16247",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dispute form
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "অভিযোগ বা বিরোধ দাখিল করুন" else "Submit a Dispute or Ticket",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == AppLanguage.BANGLA) "অভিযোগের ধরন" else "Issue Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                var expanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category, fontSize = 13.sp)
                        Text("▼", fontSize = 10.sp)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = txIdInput,
                    onValueChange = { txIdInput = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "লেনদেন আইডি (যদি থাকে)" else "Related Transaction ID (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("ticket_txid_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "বিস্তারিত বিবরণ লিখুন" else "Describe Your Issue") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("ticket_msg_input")
                )

                if (submissionSuccess != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = submissionSuccess!!, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (messageInput.length < 5) return@Button
                        isSubmitting = true
                        coroutineScope.launch {
                            val newTicket = SupportTicket(
                                ticketId = "TKT-" + UUID.randomUUID().toString().take(8).uppercase(),
                                userId = user.userId,
                                userName = user.fullName,
                                userPhone = user.phoneNumber,
                                category = category,
                                message = messageInput,
                                transactionId = txIdInput,
                                status = "OPEN"
                            )
                            val res = repository.submitSupportTicket(newTicket)
                            isSubmitting = false
                            res.fold(
                                onSuccess = {
                                    submissionSuccess = if (language == AppLanguage.BANGLA)
                                        "আপনার টিকিট সফলভাবে জমা দেওয়া হয়েছে! টিকিট নম্বর: ${newTicket.ticketId}"
                                    else "Ticket submitted successfully! ID: ${newTicket.ticketId}"
                                    messageInput = ""
                                    txIdInput = ""
                                },
                                onFailure = {
                                    submissionSuccess = "Failed: ${it.localizedMessage}"
                                }
                            )
                        }
                    },
                    enabled = !isSubmitting && messageInput.length >= 5,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (language == AppLanguage.BANGLA) "টিকিট পাঠান" else "Submit Ticket")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Tickets
        if (userTickets.isNotEmpty()) {
            Text(
                text = if (language == AppLanguage.BANGLA) "আপনার দাখিলকৃত অভিযোগসমূহ" else "Your Submitted Tickets",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            userTickets.forEach { t ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = t.ticketId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = t.status, color = EmeraldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(text = t.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = t.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
