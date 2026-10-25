package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    user: User,
    wallet: Wallet?,
    repository: BankingRepository,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToKyc: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isChangingPin by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // User Profile Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.fullName.ifEmpty { "ShadhinPay User" },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (user.kycStatus == "verified") StatusSuccess.copy(alpha = 0.15f)
                    else AmberAccent.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { onNavigateToKyc() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (user.kycStatus == "verified") Icons.Default.Verified else Icons.Default.Shield,
                            contentDescription = "Status",
                            tint = if (user.kycStatus == "verified") StatusSuccess else AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user.kycStatus == "verified") (if (language == AppLanguage.BANGLA) "পরিচয় যাচাইকৃত (NID Verified)" else "KYC Verified")
                            else (if (language == AppLanguage.BANGLA) "পরিচয় যাচাই করুন (Unverified)" else "Complete KYC"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (user.kycStatus == "verified") StatusSuccess else AmberAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction Limits Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "লেনদেন সীমা (বাংলাদেশ ব্যাংক অনুমোদিত)" else "Regulatory Account Limits",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (language == AppLanguage.BANGLA) "দৈনিক লেনদেন সীমা" else "Daily Limit", style = MaterialTheme.typography.bodySmall)
                    Text(Localization.formatTaka(wallet?.dailyLimit ?: 50000.0, language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (language == AppLanguage.BANGLA) "মাসিক লেনদেন সীমা" else "Monthly Limit", style = MaterialTheme.typography.bodySmall)
                    Text(Localization.formatTaka(wallet?.monthlyLimit ?: 200000.0, language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (language == AppLanguage.BANGLA) "আজকের ব্যবহৃত" else "Spent Today", style = MaterialTheme.typography.bodySmall)
                    Text(Localization.formatTaka(wallet?.spentToday ?: 0.0, language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings list
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Language Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (language == AppLanguage.BANGLA) "ভাষা (Language)" else "Language", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = language == AppLanguage.ENGLISH,
                            onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = language == AppLanguage.BANGLA,
                            onClick = { onLanguageChange(AppLanguage.BANGLA) },
                            label = { Text("বাংলা") }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Dark mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode", tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (language == AppLanguage.BANGLA) "ডার্ক মোড" else "Dark Theme", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onDarkModeToggle(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Change PIN dialog toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChangingPin = !isChangingPin }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pin, contentDescription = "PIN", tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (language == AppLanguage.BANGLA) "লেনদেন পিন পরিবর্তন" else "Change Transaction PIN", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Expand")
                    }
                }

                if (isChangingPin) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                            label = { Text(if (language == AppLanguage.BANGLA) "নতুন ৪ ডিজিটের পিন" else "New 4-digit PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newPinInput.length == 4) {
                                    coroutineScope.launch {
                                        repository.updateUserProfile(
                                            userId = user.userId,
                                            updates = mapOf("pinHash" to repository.hashPin(newPinInput))
                                        )
                                        pinMessage = if (language == AppLanguage.BANGLA) "পিন সফলভাবে পরিবর্তিত হয়েছে!" else "PIN updated successfully!"
                                        isChangingPin = false
                                        newPinInput = ""
                                    }
                                }
                            },
                            enabled = newPinInput.length == 4
                        ) {
                            Text(if (language == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save PIN")
                        }
                        if (pinMessage != null) {
                            Text(text = pinMessage!!, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Help and Support
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSupport() }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupportAgent, contentDescription = "Support", tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Localization.tr("support", language), style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Admin Dashboard toggle (Allows promotion to admin for testing the full admin panel!)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (user.role != "admin") {
                                coroutineScope.launch {
                                    repository.updateUserProfile(user.userId, mapOf("role" to "admin"))
                                }
                            }
                            onNavigateToAdmin()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = AmberAccent)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Localization.tr("admin_panel", language),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "মনিটরিং ও রিভার্সাল নিয়ন্ত্রণ কক্ষ" else "Management & Auditing Tools",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Admin Portal")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sign Out Button
        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sign_out_button")
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Sign Out", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.tr("logout", language), fontWeight = FontWeight.Bold)
        }
    }
}
