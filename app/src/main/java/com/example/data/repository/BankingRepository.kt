package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class BankingRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing ShadhinPay.")
    }

    fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest("SHADHIN_SALT_$pin".toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<User?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun createUserProfile(user: User) {
        val docRef = db.collection("users").document(user.userId)
        val payload = mapOf(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "role" to user.role,
            "status" to user.status,
            "kycStatus" to user.kycStatus,
            "nidNumber" to user.nidNumber,
            "pinHash" to user.pinHash,
            "biometricsEnabled" to user.biometricsEnabled,
            "preferredLanguage" to user.preferredLanguage,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            initializeWalletIfAbsent(user.userId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        val docRef = db.collection("users").document(userId)
        val payload = updates + mapOf("updatedAt" to FieldValue.serverTimestamp())
        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Wallet ---

    fun observeWallet(userId: String): Flow<Wallet?> = callbackFlow {
        val docRef = db.collection("wallets").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val wallet = snapshot.toObject(Wallet::class.java)
                trySend(wallet)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun initializeWalletIfAbsent(userId: String) {
        val walletRef = db.collection("wallets").document(userId)
        val snapshot = walletRef.get().await()
        if (!snapshot.exists()) {
            val initialWallet = mapOf(
                "walletId" to userId,
                "userId" to userId,
                "balance" to 25000.0, // Welcome promotional starting balance in BDT for testing
                "pendingBalance" to 0.0,
                "currency" to "BDT",
                "dailyLimit" to 50000.0,
                "monthlyLimit" to 200000.0,
                "spentToday" to 0.0,
                "spentThisMonth" to 0.0,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            walletRef.set(initialWallet).await()
        }
    }

    // --- Transactions Ledger ---

    fun observeTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val query = db.collection("transactions")
            .whereEqualTo("senderId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "transactions")
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun observeAllTransactionsForAdmin(): Flow<List<Transaction>> = callbackFlow {
        val query = db.collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "transactions")
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun observeAllUsersForAdmin(): Flow<List<User>> = callbackFlow {
        val query = db.collection("users").limit(50)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "users")
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // --- Atomic Financial Operations ---

    suspend fun executeTransfer(
        senderUser: User,
        recipientName: String,
        recipientPhoneOrId: String,
        type: String,
        amount: Double,
        fee: Double,
        pin: String,
        reference: String = "",
        metadata: String = ""
    ): Result<Transaction> {
        val senderId = requireUserId()
        if (senderUser.status != "active") {
            return Result.failure(IllegalStateException("Account is ${senderUser.status}. Transactions are blocked."))
        }
        if (senderUser.pinHash.isNotEmpty() && hashPin(pin) != senderUser.pinHash) {
            return Result.failure(IllegalArgumentException("Invalid 4-digit Transaction PIN. Please try again."))
        }
        val totalAmount = amount + fee

        val txId = "TRX-" + UUID.randomUUID().toString().take(10).uppercase()
        val idempotencyKey = UUID.randomUUID().toString()

        val senderWalletRef = db.collection("wallets").document(senderId)
        val txRef = db.collection("transactions").document(txId)

        return try {
            db.runTransaction { transaction ->
                val senderWalletSnapshot = transaction.get(senderWalletRef)
                if (!senderWalletSnapshot.exists()) {
                    throw IllegalStateException("Sender wallet not initialized.")
                }
                val currentBalance = (senderWalletSnapshot.get("balance") as? Number)?.toDouble() ?: 0.0
                val dailyLimit = (senderWalletSnapshot.get("dailyLimit") as? Number)?.toDouble() ?: 50000.0
                val spentToday = (senderWalletSnapshot.get("spentToday") as? Number)?.toDouble() ?: 0.0

                if (currentBalance < totalAmount) {
                    throw IllegalStateException("Insufficient balance. Available: ৳$currentBalance, Required: ৳$totalAmount")
                }
                if (spentToday + amount > dailyLimit) {
                    throw IllegalStateException("Daily transaction limit exceeded (Limit: ৳$dailyLimit).")
                }

                val newBalance = currentBalance - totalAmount
                val newSpentToday = spentToday + amount

                // Deduct sender wallet
                transaction.update(
                    senderWalletRef,
                    mapOf(
                        "balance" to newBalance,
                        "spentToday" to newSpentToday,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                // Write immutable transaction
                val txPayload = mapOf(
                    "transactionId" to txId,
                    "idempotencyKey" to idempotencyKey,
                    "senderId" to senderId,
                    "senderName" to senderUser.fullName,
                    "senderPhone" to senderUser.phoneNumber,
                    "recipientId" to recipientPhoneOrId,
                    "recipientName" to recipientName,
                    "recipientPhone" to recipientPhoneOrId,
                    "type" to type,
                    "amount" to amount,
                    "fee" to fee,
                    "totalAmount" to totalAmount,
                    "currency" to "BDT",
                    "status" to "SUCCESS",
                    "reference" to reference,
                    "metadata" to metadata,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                transaction.set(txRef, txPayload)

                null
            }.await()

            // Send in-app notification
            sendNotification(
                userId = senderId,
                title = "Payment Successful: ৳${"%.2f".format(amount)}",
                body = "$type of ৳${"%.2f".format(amount)} to $recipientName was successful. Ref: $txId",
                type = "TRANSACTION"
            )

            Result.success(
                Transaction(
                    transactionId = txId,
                    idempotencyKey = idempotencyKey,
                    senderId = senderId,
                    senderName = senderUser.fullName,
                    senderPhone = senderUser.phoneNumber,
                    recipientId = recipientPhoneOrId,
                    recipientName = recipientName,
                    recipientPhone = recipientPhoneOrId,
                    type = type,
                    amount = amount,
                    fee = fee,
                    totalAmount = totalAmount,
                    currency = "BDT",
                    status = "SUCCESS",
                    reference = reference,
                    metadata = metadata,
                    createdAt = Timestamp.now()
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "transactions/$txId")
            Result.failure(e)
        }
    }

    suspend fun executeAddMoney(
        user: User,
        sourceMethod: String, // Bank Card, City Bank, DBBL, BKash, Agent Cash-In
        amount: Double,
        reference: String = ""
    ): Result<Transaction> {
        val uid = requireUserId()
        val txId = "TRX-ADD-" + UUID.randomUUID().toString().take(8).uppercase()
        val walletRef = db.collection("wallets").document(uid)
        val txRef = db.collection("transactions").document(txId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(walletRef)
                val currentBalance = if (snapshot.exists()) {
                    (snapshot.get("balance") as? Number)?.toDouble() ?: 0.0
                } else 0.0

                val newBalance = currentBalance + amount

                if (snapshot.exists()) {
                    transaction.update(
                        walletRef,
                        mapOf(
                            "balance" to newBalance,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        walletRef,
                        mapOf(
                            "walletId" to uid,
                            "userId" to uid,
                            "balance" to newBalance,
                            "pendingBalance" to 0.0,
                            "currency" to "BDT",
                            "dailyLimit" to 50000.0,
                            "monthlyLimit" to 200000.0,
                            "spentToday" to 0.0,
                            "spentThisMonth" to 0.0,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val txPayload = mapOf(
                    "transactionId" to txId,
                    "idempotencyKey" to UUID.randomUUID().toString(),
                    "senderId" to uid,
                    "senderName" to user.fullName,
                    "senderPhone" to user.phoneNumber,
                    "recipientId" to uid,
                    "recipientName" to "Wallet Deposit ($sourceMethod)",
                    "recipientPhone" to user.phoneNumber,
                    "type" to "ADD_MONEY",
                    "amount" to amount,
                    "fee" to 0.0,
                    "totalAmount" to amount,
                    "currency" to "BDT",
                    "status" to "SUCCESS",
                    "reference" to reference,
                    "metadata" to "Source: $sourceMethod",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                transaction.set(txRef, txPayload)
                null
            }.await()

            sendNotification(
                userId = uid,
                title = "Money Added: ৳${"%.2f".format(amount)}",
                body = "৳${"%.2f".format(amount)} was added to your wallet via $sourceMethod. Ref: $txId",
                type = "TRANSACTION"
            )

            Result.success(
                Transaction(
                    transactionId = txId,
                    senderId = uid,
                    senderName = user.fullName,
                    recipientName = "Wallet Deposit ($sourceMethod)",
                    type = "ADD_MONEY",
                    amount = amount,
                    fee = 0.0,
                    totalAmount = amount,
                    status = "SUCCESS",
                    createdAt = Timestamp.now()
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "transactions/$txId")
            Result.failure(e)
        }
    }

    // --- Admin Operations ---

    suspend fun adminUpdateUserStatus(userId: String, newStatus: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            sendNotification(
                userId = userId,
                title = "Account Status Updated",
                body = "Your ShadhinPay account status has been updated to: $newStatus",
                type = "SECURITY"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateKycStatus(userId: String, newKycStatus: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            docRef.update(
                mapOf(
                    "kycStatus" to newKycStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            sendNotification(
                userId = userId,
                title = "KYC Verification Update",
                body = "Your identity verification status has been marked as: $newKycStatus",
                type = "SECURITY"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminReverseTransaction(transactionId: String): Result<Unit> {
        val txRef = db.collection("transactions").document(transactionId)
        return try {
            db.runTransaction { transaction ->
                val txSnapshot = transaction.get(txRef)
                if (!txSnapshot.exists()) {
                    throw IllegalStateException("Transaction not found.")
                }
                val currentStatus = txSnapshot.getString("status") ?: ""
                if (currentStatus != "SUCCESS") {
                    throw IllegalStateException("Only SUCCESS transactions can be reversed.")
                }
                val senderId = txSnapshot.getString("senderId") ?: ""
                val totalAmount = (txSnapshot.get("totalAmount") as? Number)?.toDouble() ?: 0.0

                val senderWalletRef = db.collection("wallets").document(senderId)
                val senderWalletSnapshot = transaction.get(senderWalletRef)
                if (senderWalletSnapshot.exists()) {
                    val currentBal = (senderWalletSnapshot.get("balance") as? Number)?.toDouble() ?: 0.0
                    transaction.update(
                        senderWalletRef,
                        mapOf(
                            "balance" to (currentBal + totalAmount),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                transaction.update(
                    txRef,
                    mapOf(
                        "status" to "REVERSED",
                        "metadata" to (txSnapshot.getString("metadata") ?: "") + " [Admin Reversal Issued]"
                    )
                )
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, txRef.path)
            Result.failure(e)
        }
    }

    // --- Beneficiaries ---

    fun observeBeneficiaries(userId: String): Flow<List<Beneficiary>> = callbackFlow {
        val query = db.collection("beneficiaries").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "beneficiaries")
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Beneficiary::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addBeneficiary(beneficiary: Beneficiary): Result<Unit> {
        val docRef = db.collection("beneficiaries").document(beneficiary.beneficiaryId)
        val payload = mapOf(
            "beneficiaryId" to beneficiary.beneficiaryId,
            "userId" to beneficiary.userId,
            "name" to beneficiary.name,
            "phoneOrAccount" to beneficiary.phoneOrAccount,
            "type" to beneficiary.type,
            "bankName" to beneficiary.bankName,
            "branch" to beneficiary.branch,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteBeneficiary(beneficiaryId: String): Result<Unit> {
        val docRef = db.collection("beneficiaries").document(beneficiaryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Support Tickets ---

    fun observeSupportTickets(userId: String): Flow<List<SupportTicket>> = callbackFlow {
        val query = db.collection("supportTickets").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "supportTickets")
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(SupportTicket::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun submitSupportTicket(ticket: SupportTicket): Result<Unit> {
        val docRef = db.collection("supportTickets").document(ticket.ticketId)
        val payload = mapOf(
            "ticketId" to ticket.ticketId,
            "userId" to ticket.userId,
            "userName" to ticket.userName,
            "userPhone" to ticket.userPhone,
            "category" to ticket.category,
            "message" to ticket.message,
            "transactionId" to ticket.transactionId,
            "status" to ticket.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Notifications ---

    fun observeNotifications(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        val query = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "notifications")
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(NotificationItem::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun sendNotification(userId: String, title: String, body: String, type: String) {
        val notifId = UUID.randomUUID().toString()
        val docRef = db.collection("notifications").document(notifId)
        val payload = mapOf(
            "notificationId" to notifId,
            "userId" to userId,
            "title" to title,
            "body" to body,
            "type" to type,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    suspend fun markNotificationRead(notificationId: String) {
        val docRef = db.collection("notifications").document(notificationId)
        try {
            docRef.update("read", true).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }
}
