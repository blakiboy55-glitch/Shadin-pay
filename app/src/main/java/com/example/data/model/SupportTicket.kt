package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class SupportTicket(
    val ticketId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val category: String = "Transaction Dispute",
    val message: String = "",
    val transactionId: String = "",
    val status: String = "OPEN", // OPEN, INVESTIGATING, RESOLVED, CLOSED
    @ServerTimestamp val createdAt: Timestamp? = null
)
