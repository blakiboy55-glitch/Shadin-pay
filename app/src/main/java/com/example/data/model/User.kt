package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val userId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val role: String = "customer", // customer, merchant, agent, admin
    val status: String = "active", // active, restricted, suspended
    val kycStatus: String = "unverified", // unverified, pending, verified
    val nidNumber: String = "",
    val pinHash: String = "",
    val biometricsEnabled: Boolean = false,
    val preferredLanguage: String = "en", // en or bn
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
