package com.example.budgetbee.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class SharedPrefHelper(private val context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("BudgetBeePrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var lastBudgetAlertShown = false

    var currency: String
        get() = sharedPref.getString("currency", "$") ?: "$"
        set(value) = sharedPref.edit().putString("currency", value).apply()

    var monthlyBudget: Double
        get() = sharedPref.getFloat("monthlyBudget", 0f).toDouble()
        set(value) = sharedPref.edit().putFloat("monthlyBudget", value.toFloat()).apply()

    var dailyReminderEnabled: Boolean
        get() = sharedPref.getBoolean("dailyReminder", true)
        set(value) = sharedPref.edit().putBoolean("dailyReminder", value).apply()

    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPref.edit().putString("transactions", json).apply()
        checkBudget(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPref.getString("transactions", null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun checkBudget(transactions: List<Transaction>) {
        val budget = monthlyBudget
        if (budget <= 0) return

        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyExpenses = transactions
            .filter { 
                val transactionMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(it.date)
                transactionMonth == currentMonth && it.type == TransactionType.EXPENSE 
            }
            .sumOf { it.amount }

        val progress = (monthlyExpenses / budget * 100).toInt()

        if (progress >= 80 && !lastBudgetAlertShown) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showBudgetAlert(
                "Budget Warning",
                "You've spent $progress% of your monthly budget"
            )
            lastBudgetAlertShown = true
        } else if (progress < 80) {
            lastBudgetAlertShown = false
        }
    }

    fun getFormattedAmount(amount: Double): String {
        return String.format(Locale.getDefault(), "$currency%.2f", amount)
    }

    fun getMonthlyExpenses(): Double {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        return getTransactions()
            .filter { 
                val transactionMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(it.date)
                transactionMonth == currentMonth && it.type == TransactionType.EXPENSE 
            }
            .sumOf { it.amount }
    }
}