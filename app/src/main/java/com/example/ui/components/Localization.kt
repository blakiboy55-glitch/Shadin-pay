package com.example.ui.components

import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা")
}

object Localization {
    fun formatTaka(amount: Double, language: AppLanguage = AppLanguage.ENGLISH): String {
        val formattedNumber = String.format(Locale.US, "%,.2f", amount)
        return if (language == AppLanguage.BANGLA) {
            val bengaliDigits = mapOf(
                '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
                ',' to ',', '.' to '.'
            )
            val converted = formattedNumber.map { bengaliDigits[it] ?: it }.joinToString("")
            "৳ $converted"
        } else {
            "৳ $formattedNumber"
        }
    }

    fun tr(key: String, language: AppLanguage): String {
        val isBn = language == AppLanguage.BANGLA
        return when (key) {
            "app_name" -> if (isBn) "স্বাধীন পে" else "ShadhinPay"
            "app_tagline" -> if (isBn) "ডিজিটাল আর্থিক স্বাধীনতা ও নিরাপদ ব্যাংকিং" else "Digital Financial Freedom & Secure Banking"
            "available_balance" -> if (isBn) "সর্বমোট ব্যালেন্স" else "Available Balance"
            "tap_for_balance" -> if (isBn) "ব্যালেন্স দেখতে ট্যাপ করুন" else "Tap for Balance"
            "send_money" -> if (isBn) "সেন্ড মানি" else "Send Money"
            "add_money" -> if (isBn) "অ্যাড মানি" else "Add Money"
            "cash_out" -> if (isBn) "ক্যাশ আউট" else "Cash Out"
            "recharge" -> if (isBn) "মোবাইল রিচার্জ" else "Mobile Recharge"
            "merchant_pay" -> if (isBn) "পেমেন্ট" else "Merchant Pay"
            "pay_bill" -> if (isBn) "বিল পে" else "Pay Bill"
            "bank_transfer" -> if (isBn) "ব্যাংক ট্রান্সফার" else "Bank Transfer"
            "qr_pay" -> if (isBn) "কিউআর কোড" else "QR Pay"
            "transactions" -> if (isBn) "লেনদেন ইতিহাস" else "Transactions"
            "favorite_contacts" -> if (isBn) "প্রিয় প্রাপক" else "Favorite Recipients"
            "recent_activity" -> if (isBn) "সাম্প্রতিক লেনদেন" else "Recent Activity"
            "promotions" -> if (isBn) "বিশেষ অফার ও বোনাস" else "Special Offers"
            "notifications" -> if (isBn) "বিজ্ঞপ্তি" else "Notifications"
            "support" -> if (isBn) "সহায়তা ও হেল্পলাইন" else "Help & Support"
            "profile" -> if (isBn) "আমার প্রোফাইল" else "My Profile"
            "admin_panel" -> if (isBn) "অ্যাডমিন নিয়ন্ত্রণ কক্ষ" else "Admin Dashboard"
            "kyc_verification" -> if (isBn) "এনআইডি ও পরিচয় যাচাই" else "NID / KYC Verification"
            "enter_pin" -> if (isBn) "৪ ডিজিটের গোপন পিন দিন" else "Enter 4-Digit Secret PIN"
            "confirm_transfer" -> if (isBn) "লেনদেন নিশ্চিত করতে চাপুন" else "Tap to Confirm Transfer"
            "enter_amount" -> if (isBn) "টাকার পরিমাণ লিখুন" else "Enter Amount in BDT"
            "fee" -> if (isBn) "চার্জ" else "Fee"
            "total" -> if (isBn) "মোট টাকা" else "Total Amount"
            "success" -> if (isBn) "সফল হয়েছে" else "Success"
            "failed" -> if (isBn) "ব্যর্থ হয়েছে" else "Failed"
            "pending" -> if (isBn) "প্রক্রিয়াধীন" else "Pending"
            "reversed" -> if (isBn) "রিভার্সড / ফেরত" else "Reversed"
            "nav_home" -> if (isBn) "হোম" else "Home"
            "nav_payments" -> if (isBn) "পেমেন্টস" else "Payments"
            "nav_history" -> if (isBn) "হিস্ট্রি" else "History"
            "nav_inbox" -> if (isBn) "ইনবক্স" else "Inbox"
            "nav_profile" -> if (isBn) "প্রোফাইল" else "Profile"
            "logout" -> if (isBn) "লগ আউট" else "Sign Out"
            else -> key
        }
    }
}
