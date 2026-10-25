package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldPrimary

@Composable
fun QrScannerScreen(
    user: User,
    language: AppLanguage,
    onScannedMerchant: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Scan, 1: My QR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("qr_scanner_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (language == AppLanguage.BANGLA) "কিউআর স্ক্যান করুন" else "Scan QR") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (language == AppLanguage.BANGLA) "আমার কিউআর কোড" else "My QR Code") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTab == 0) {
            // Scanner Viewfinder Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "ক্যামেরা ফ্রেমের ভেতর কিউআর কোড ধরুন" else "Align QR Code within the viewfinder frame",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Scanner Viewfinder box
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .border(3.dp, EmeraldPrimary, RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F1B16)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner",
                            tint = AmberAccent,
                            modifier = Modifier.size(90.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (language == AppLanguage.BANGLA) "দ্রুত পরীক্ষার জন্য নিচে ট্যাপ করুন:" else "Tap sample merchant to simulate quick scan:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onScannedMerchant("01700998877", "Aarong Flagship Outlet") },
                            modifier = Modifier.weight(1f).testTag("sample_qr_aarong")
                        ) {
                            Text("Aarong Outlet", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onScannedMerchant("01819876543", "Bismillah Agent Point") },
                            modifier = Modifier.weight(1f).testTag("sample_qr_agent")
                        ) {
                            Text("Agent Point", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // My QR Code
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stylized QR code representation
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Personal QR",
                                tint = Color.Black,
                                modifier = Modifier.size(170.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (language == AppLanguage.BANGLA)
                            "এই কিউআর কোড স্ক্যান করে অন্য যে কেউ সরাসরি আপনার ওয়ালেটে টাকা পাঠাতে পারবে"
                        else "Anyone can scan this personal QR code to instantly send money to your ShadhinPay wallet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
