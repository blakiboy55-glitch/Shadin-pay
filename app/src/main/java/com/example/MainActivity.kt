package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Beneficiary
import com.example.data.model.NotificationItem
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.Wallet
import com.example.data.repository.BankingRepository
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                AppRoot(
                    isDarkMode = isDarkMode,
                    onDarkModeToggle = { isDarkMode = it }
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var language by remember { mutableStateOf(AppLanguage.ENGLISH) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        val repository = remember { BankingRepository(context) }
        AuthScreen(
            repository = repository,
            language = language,
            onAuthComplete = {
                currentUser = FirebaseAuth.getInstance().currentUser
            }
        )
    } else {
        MainAuthenticatedApp(
            userId = currentUser!!.uid,
            language = language,
            onLanguageToggle = {
                language = if (language == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
            },
            onLanguageChange = { language = it },
            isDarkMode = isDarkMode,
            onDarkModeToggle = onDarkModeToggle,
            onSignOut = {
                FirebaseAuth.getInstance().signOut()
                currentUser = null
            }
        )
    }
}

@Composable
fun MainAuthenticatedApp(
    userId: String,
    language: AppLanguage,
    onLanguageToggle: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember(userId) { BankingRepository(context) }

    var currentScreen by remember { mutableStateOf("home") }
    var userProfile by remember { mutableStateOf<User?>(null) }
    var wallet by remember { mutableStateOf<Wallet?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var beneficiaries by remember { mutableStateOf<List<Beneficiary>>(emptyList()) }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    var activeReceiptTransaction by remember { mutableStateOf<Transaction?>(null) }
    var prefilledRecipient by remember { mutableStateOf<Beneficiary?>(null) }

    // Start background listeners
    LaunchedEffect(userId) {
        repository.initializeWalletIfAbsent(userId)

        coroutineScope.launch {
            repository.observeUserProfile(userId).collect { user ->
                userProfile = user ?: User(
                    userId = userId,
                    fullName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Account Holder",
                    phoneNumber = "+8801712345678",
                    email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    role = "customer",
                    status = "active",
                    kycStatus = "unverified"
                )
            }
        }

        coroutineScope.launch {
            repository.observeWallet(userId).collect { w ->
                wallet = w
            }
        }

        coroutineScope.launch {
            repository.observeTransactions(userId).collect { txs ->
                transactions = txs
            }
        }

        coroutineScope.launch {
            repository.observeBeneficiaries(userId).collect { bens ->
                beneficiaries = bens
            }
        }

        coroutineScope.launch {
            repository.observeNotifications(userId).collect { notifs ->
                notifications = notifs
            }
        }
    }

    // Predictive Back Handling
    BackHandler(enabled = currentScreen != "home") {
        currentScreen = "home"
    }

    val unreadNotifs = remember(notifications) { notifications.count { !it.read } }
    val isAdmin = userProfile?.role == "admin"

    Scaffold(
        topBar = {
            ShadhinTopBar(
                title = when (currentScreen) {
                    "home" -> Localization.tr("app_name", language)
                    "payments" -> Localization.tr("nav_payments", language)
                    "send_money" -> Localization.tr("send_money", language)
                    "add_money" -> Localization.tr("add_money", language)
                    "cash_out" -> Localization.tr("cash_out", language)
                    "recharge" -> Localization.tr("recharge", language)
                    "merchant" -> Localization.tr("merchant_pay", language)
                    "bill_pay" -> Localization.tr("pay_bill", language)
                    "bank_transfer" -> Localization.tr("bank_transfer", language)
                    "qr_pay" -> Localization.tr("qr_pay", language)
                    "transactions" -> Localization.tr("transactions", language)
                    "inbox" -> Localization.tr("nav_inbox", language)
                    "kyc" -> Localization.tr("kyc_verification", language)
                    "support" -> Localization.tr("support", language)
                    "admin" -> Localization.tr("admin_panel", language)
                    "profile" -> Localization.tr("profile", language)
                    else -> Localization.tr("app_name", language)
                },
                language = language,
                onLanguageToggle = onLanguageToggle,
                showBackButton = currentScreen != "home",
                onBackClick = { currentScreen = "home" },
                unreadNotificationCount = unreadNotifs,
                onNotificationClick = { currentScreen = "inbox" },
                isAdmin = isAdmin,
                onAdminClick = { currentScreen = "admin" }
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { currentScreen = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(Localization.tr("nav_home", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_home_btn")
                )
                NavigationBarItem(
                    selected = currentScreen == "payments" || currentScreen in listOf("send_money", "add_money", "cash_out", "recharge", "merchant", "bill_pay", "bank_transfer", "qr_pay"),
                    onClick = { currentScreen = "payments" },
                    icon = { Icon(Icons.Default.Payment, contentDescription = "Payments") },
                    label = { Text(Localization.tr("nav_payments", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_payments_btn")
                )
                NavigationBarItem(
                    selected = currentScreen == "transactions",
                    onClick = { currentScreen = "transactions" },
                    icon = { Icon(Icons.Default.History, contentDescription = "Transactions") },
                    label = { Text(Localization.tr("nav_history", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_transactions_btn")
                )
                NavigationBarItem(
                    selected = currentScreen == "inbox",
                    onClick = { currentScreen = "inbox" },
                    icon = {
                        BadgedBox(badge = {
                            if (unreadNotifs > 0) Badge { Text(unreadNotifs.toString()) }
                        }) {
                            Icon(Icons.Default.Mail, contentDescription = "Inbox")
                        }
                    },
                    label = { Text(Localization.tr("nav_inbox", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_inbox_btn")
                )
                NavigationBarItem(
                    selected = currentScreen == "profile",
                    onClick = { currentScreen = "profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text(Localization.tr("nav_profile", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_profile_btn")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val user = userProfile ?: User(userId = userId)

            when (currentScreen) {
                "home" -> {
                    DashboardScreen(
                        user = user,
                        wallet = wallet,
                        recentTransactions = transactions,
                        beneficiaries = beneficiaries,
                        language = language,
                        onNavigateTo = { currentScreen = it },
                        onSelectTransaction = { activeReceiptTransaction = it },
                        onSelectBeneficiary = { ben ->
                            prefilledRecipient = ben
                            currentScreen = "send_money"
                        }
                    )
                }

                "payments" -> {
                    // Quick Payments Hub
                    PaymentsHubScreen(
                        language = language,
                        onNavigateTo = { currentScreen = it }
                    )
                }

                "send_money" -> {
                    SendMoneyScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        beneficiaries = beneficiaries,
                        prefilledRecipient = prefilledRecipient,
                        language = language,
                        onTransactionSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "add_money" -> {
                    AddMoneyScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "cash_out" -> {
                    CashOutScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "recharge" -> {
                    MobileRechargeScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "merchant" -> {
                    MerchantPaymentScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "bill_pay" -> {
                    BillPaymentScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "bank_transfer" -> {
                    BankTransferScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onSuccess = { tx ->
                            activeReceiptTransaction = tx
                            currentScreen = "home"
                        }
                    )
                }

                "qr_pay" -> {
                    QrScannerScreen(
                        user = user,
                        language = language,
                        onScannedMerchant = { _, _ ->
                            currentScreen = "merchant"
                        }
                    )
                }

                "transactions" -> {
                    TransactionsScreen(
                        transactions = transactions,
                        language = language,
                        onSelectTransaction = { activeReceiptTransaction = it }
                    )
                }

                "inbox" -> {
                    NotificationScreen(
                        notifications = notifications,
                        language = language,
                        onNotificationClick = { notif ->
                            coroutineScope.launch {
                                repository.markNotificationRead(notif.notificationId)
                            }
                        }
                    )
                }

                "kyc" -> {
                    KycScreen(
                        user = user,
                        repository = repository,
                        language = language,
                        onKycSubmitted = {
                            currentScreen = "home"
                        }
                    )
                }

                "support" -> {
                    SupportScreen(
                        user = user,
                        repository = repository,
                        language = language
                    )
                }

                "admin" -> {
                    AdminDashboardScreen(
                        adminUser = user,
                        repository = repository,
                        language = language,
                        onBack = { currentScreen = "home" }
                    )
                }

                "profile" -> {
                    ProfileScreen(
                        user = user,
                        wallet = wallet,
                        repository = repository,
                        language = language,
                        onLanguageChange = onLanguageChange,
                        isDarkMode = isDarkMode,
                        onDarkModeToggle = onDarkModeToggle,
                        onNavigateToAdmin = { currentScreen = "admin" },
                        onNavigateToKyc = { currentScreen = "kyc" },
                        onNavigateToSupport = { currentScreen = "support" },
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }

    // Receipt Bottom Sheet
    TransactionReceiptSheet(
        transaction = activeReceiptTransaction,
        language = language,
        onDismiss = { activeReceiptTransaction = null }
    )
}

@Composable
fun PaymentsHubScreen(
    language: AppLanguage,
    onNavigateTo: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("payments_hub_screen")
    ) {
        Text(
            text = if (language == AppLanguage.BANGLA) "আর্থিক সেবাসমূহ" else "Financial Services & Transfers",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                listOf(
                    Triple(Localization.tr("send_money", language), Icons.Default.Send, "send_money"),
                    Triple(Localization.tr("add_money", language), Icons.Default.AddCard, "add_money"),
                    Triple(Localization.tr("cash_out", language), Icons.Default.Payments, "cash_out"),
                    Triple(Localization.tr("recharge", language), Icons.Default.PhoneAndroid, "recharge"),
                    Triple(Localization.tr("merchant_pay", language), Icons.Default.Storefront, "merchant"),
                    Triple(Localization.tr("pay_bill", language), Icons.Default.ReceiptLong, "bill_pay"),
                    Triple(Localization.tr("bank_transfer", language), Icons.Default.AccountBalance, "bank_transfer"),
                    Triple(Localization.tr("qr_pay", language), Icons.Default.QrCodeScanner, "qr_pay")
                ).forEach { (label, icon, route) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTo(route) }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
