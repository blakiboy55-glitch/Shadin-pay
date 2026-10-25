package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Wallet(
    val walletId: String = "",
    val userId: String = "",
    val balance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val currency: String = "BDT",
    val dailyLimit: Double = 25000.0,
    val monthlyLimit: Double = 100000.0,
    val spentToday: Double = 0.0,
    val spentThisMonth: Double = 0.0,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
