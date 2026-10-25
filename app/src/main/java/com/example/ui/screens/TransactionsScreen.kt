package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import com.example.ui.theme.EmeraldPrimary

@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    language: AppLanguage,
    onSelectTransaction: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val filterTypes = listOf(
        "ALL" to if (language == AppLanguage.BANGLA) "সকল" else "All",
        "SEND_MONEY" to if (language == AppLanguage.BANGLA) "সেন্ড মানি" else "Send Money",
        "ADD_MONEY" to if (language == AppLanguage.BANGLA) "অ্যাড মানি" else "Add Money",
        "CASH_OUT" to if (language == AppLanguage.BANGLA) "ক্যাশ আউট" else "Cash Out",
        "MOBILE_RECHARGE" to if (language == AppLanguage.BANGLA) "রিচার্জ" else "Recharge",
        "MERCHANT_PAYMENT" to if (language == AppLanguage.BANGLA) "মার্চেন্ট" else "Merchant",
        "BILL_PAY" to if (language == AppLanguage.BANGLA) "বিল পে" else "Bill Pay",
        "BANK_TRANSFER" to if (language == AppLanguage.BANGLA) "ব্যাংক" else "Bank"
    )

    val filteredList = remember(transactions, searchQuery, selectedTypeFilter) {
        transactions.filter { tx ->
            val matchesType = selectedTypeFilter == "ALL" || tx.type == selectedTypeFilter
            val matchesQuery = searchQuery.isEmpty() ||
                    tx.transactionId.contains(searchQuery, ignoreCase = true) ||
                    tx.recipientName.contains(searchQuery, ignoreCase = true) ||
                    tx.reference.contains(searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("transactions_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (language == AppLanguage.BANGLA) "লেনদেন আইডি বা প্রাপক খুঁজুন..." else "Search by TxID or recipient...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_transactions_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filterTypes) { (typeKey, typeLabel) ->
                FilterChip(
                    selected = selectedTypeFilter == typeKey,
                    onClick = { selectedTypeFilter = typeKey },
                    label = { Text(typeLabel, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "No Results",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো লেনদেন পাওয়া যায়নি" else "No matching transactions found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredList) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        language = language,
                        onClick = { onSelectTransaction(tx) }
                    )
                }
            }
        }
    }
}
