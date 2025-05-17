package com.example.budgetbee.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.budgetbee.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.text.NumberFormat
import java.util.Currency

class SharedPrefHelper(private val context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("BudgetBeePrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var currency: String
        get() = sharedPref.getString("currency", "USD") ?: "USD"
        set(value) = sharedPref.edit().putString("currency", value).apply()

    var monthlyBudget: Double
        get() = sharedPref.getFloat("monthlyBudget", 0f).toDouble()
        set(value) = sharedPref.edit().putFloat("monthlyBudget", value.toFloat()).apply()

    var dailyReminderEnabled: Boolean
        get() = sharedPref.getBoolean("dailyReminder", true)
        set(value) = sharedPref.edit().putBoolean("dailyReminder", value).apply()

    var loggedInUserId: Long
        get() = sharedPref.getLong("logged_in_user_id", -1L)
        set(value) = sharedPref.edit().putLong("logged_in_user_id", value).apply()

    fun clearLoggedInUser() {
        sharedPref.edit().remove("logged_in_user_id").apply()
    }

    fun saveTransactions(transactions: List<Transaction>, userId: Long) {
        try {
            val json = gson.toJson(transactions)
            sharedPref.edit()
                .putString("transactions_$userId", json)
                .apply()
        } catch (e: Exception) {
            Log.e("SharedPrefHelper", "Error saving transactions: ", e)
        }
    }

    fun getTransactions(userId: Long): List<Transaction> {
        return try {
            val json = sharedPref.getString("transactions_$userId", null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<Transaction>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SharedPrefHelper", "Error getting transactions: ", e)
            emptyList()
        }
    }

    fun getCurrentMonthTransactions(userId: Long): List<Transaction> {
        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
        return getTransactions(userId).filter { it.date.startsWith(currentMonth) }
    }

    fun getFormattedAmount(amount: Double, currencyCode: String): String {
        val symbol = try { java.util.Currency.getInstance(currencyCode).symbol } catch (e: Exception) { "$" }
        return if (amount % 1.0 == 0.0) {
            "$symbol${amount.toInt()}"
        } else {
            "$symbol${amount}".replace(Regex("""\.0+"""), "")
        }
    }
}
