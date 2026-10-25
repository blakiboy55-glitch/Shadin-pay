package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Transaction(
    val transactionId: String = "",
    val idempotencyKey: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val type: String = "SEND_MONEY", // SEND_MONEY, ADD_MONEY, CASH_OUT, MOBILE_RECHARGE, MERCHANT_PAYMENT, BILL_PAY, BANK_TRANSFER
    val amount: Double = 0.0,
    val fee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val currency: String = "BDT",
    val status: String = "SUCCESS", // SUCCESS, PENDING, FAILED, REVERSED
    val reference: String = "",
    val metadata: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
