package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.User
import com.example.data.repository.BankingRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldPrimary
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    repository: BankingRepository,
    language: AppLanguage,
    onAuthComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }

    var isGoogleLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isSettingUpProfile by remember { mutableStateOf(false) }

    var phoneInput by remember { mutableStateOf("01712345678") }
    var pinInput by remember { mutableStateOf("1234") }
    var nameInput by remember { mutableStateOf("") }
    var isSavingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser
        if (user != null) {
            nameInput = user.displayName ?: "ShadhinPay User"
            isSettingUpProfile = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header
            Surface(
                shape = CircleShape,
                color = EmeraldPrimary,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "৳",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Localization.tr("app_name", language),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = Localization.tr("app_tagline", language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isSettingUpProfile) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "নিরাপদ লগইন ও নিবন্ধন" else "Secure Sign In & Registration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA)
                                "বাংলাদেশ ব্যাংক নীতি অনুযায়ী সুরক্ষিত গুগল অ্যাকাউন্টের মাধ্যমে সাইন ইন করুন"
                            else "Sign in securely with your Google Account verified credentials",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                isGoogleLoading = true
                                authError = null
                                performGoogleSignIn(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onSuccess = {
                                        isGoogleLoading = false
                                        val firebaseUser = auth.currentUser
                                        nameInput = firebaseUser?.displayName ?: "ShadhinPay User"
                                        isSettingUpProfile = true
                                    },
                                    onError = { err ->
                                        isGoogleLoading = false
                                        authError = err
                                    },
                                    onCancelled = {
                                        isGoogleLoading = false
                                    }
                                )
                            },
                            enabled = !isGoogleLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("google_signin_button")
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Security Shield",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "গুগল দিয়ে সাইন ইন করুন" else "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    } else {
                        // Profile setup step for Bangladeshi Phone & 4-digit PIN
                        Text(
                            text = if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট প্রোফাইল ও পিন সেটআপ" else "Account & PIN Setup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA)
                                "আপনার বাংলাদেশি মোবাইল নম্বর এবং ৪ ডিজিটের লেনদেন পিন দিন"
                            else "Link your Bangladeshi mobile number and secure 4-digit transaction PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(if (language == AppLanguage.BANGLA) "পূর্ণ নাম" else "Full Legal Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("name_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { if (it.length <= 11) phoneInput = it },
                            label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর (01XXXXXXXXX)" else "Mobile Number") },
                            leadingIcon = {
                                Text(
                                    text = "+88",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("phone_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                            label = { Text(if (language == AppLanguage.BANGLA) "৪ ডিজিটের গোপন পিন" else "4-Digit Secret PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "PIN Lock", tint = AmberAccent)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("pin_setup_field")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val currentAuth = auth.currentUser ?: return@Button
                                isSavingProfile = true
                                coroutineScope.launch {
                                    try {
                                        val formattedPhone = if (phoneInput.startsWith("+880")) phoneInput
                                        else if (phoneInput.startsWith("0")) "+88$phoneInput"
                                        else "+880$phoneInput"

                                        val newUser = User(
                                            userId = currentAuth.uid,
                                            fullName = nameInput.ifEmpty { "Account Holder" },
                                            phoneNumber = formattedPhone,
                                            email = currentAuth.email ?: "",
                                            role = "customer",
                                            status = "active",
                                            kycStatus = "unverified",
                                            pinHash = repository.hashPin(pinInput),
                                            biometricsEnabled = true,
                                            preferredLanguage = language.code
                                        )
                                        repository.createUserProfile(newUser)
                                        isSavingProfile = false
                                        onAuthComplete()
                                    } catch (e: Exception) {
                                        isSavingProfile = false
                                        authError = e.localizedMessage ?: "Failed to save profile"
                                    }
                                }
                            },
                            enabled = !isSavingProfile && pinInput.length == 4 && phoneInput.length >= 10,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_profile_button")
                        ) {
                            if (isSavingProfile) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "শুরু করুন" else "Get Started",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = authError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Trust & compliance badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "২৫৬-বিট এনক্রিপশন ও বাংলাদেশ ব্যাংক নিরাপত্তা মানসম্পন্ন"
                    else "256-bit Bank Grade Encryption & Regulatory Standards",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun performGoogleSignIn(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                onSuccess()
            } else {
                onError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign in failed")
        }
    }
}
