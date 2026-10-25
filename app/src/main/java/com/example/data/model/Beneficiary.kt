package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Beneficiary(
    val beneficiaryId: String = "",
    val userId: String = "",
    val name: String = "",
    val phoneOrAccount: String = "",
    val type: String = "MOBILE_WALLET", // MOBILE_WALLET, BANK_ACCOUNT
    val bankName: String = "",
    val branch: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
