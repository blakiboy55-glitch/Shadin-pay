package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class NotificationItem(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "TRANSACTION", // TRANSACTION, SECURITY, PROMOTION, SYSTEM
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
