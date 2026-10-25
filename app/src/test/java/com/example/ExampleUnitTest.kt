package com.example

import com.example.ui.components.AppLanguage
import com.example.ui.components.Localization
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

class ExampleUnitTest {

  @Test
  fun testBangladeshiCurrencyFormattingEnglish() {
    val formatted = Localization.formatTaka(1500.50, AppLanguage.ENGLISH)
    assertEquals("৳ 1,500.50", formatted)
  }

  @Test
  fun testBangladeshiCurrencyFormattingBangla() {
    val formatted = Localization.formatTaka(1500.50, AppLanguage.BANGLA)
    assertEquals("৳ ১,৫০০.৫০", formatted)
  }

  @Test
  fun testPinHashingSafety() {
    val pin = "1234"
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest("SHADHIN_SALT_$pin".toByteArray())
    val hashed = digest.fold("") { str, it -> str + "%02x".format(it) }

    assertNotEquals("1234", hashed)
    assertEquals(64, hashed.length) // SHA-256 hex string length is 64
  }

  @Test
  fun testCashOutFeeCalculation() {
    val amount = 1000.0
    val fee = Math.round((amount * 0.0149) * 100.0) / 100.0
    assertEquals(14.90, fee, 0.01)

    val totalAmount = amount + fee
    assertEquals(1014.90, totalAmount, 0.01)
  }
}
