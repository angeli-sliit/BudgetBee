package com.example.budgetbee.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.budgetbee.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class SharedPrefHelper(private val context: Context) {
    val sharedPref: SharedPreferences =
        context.getSharedPreferences("BudgetBeePrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var currency: String
        get() = sharedPref.getString("currency", "$") ?: "$"
        set(value) = sharedPref.edit().putString("currency", value).apply()

    var monthlyBudget: Double
        get() = sharedPref.getFloat("monthlyBudget", 0f).toDouble()
        set(value) = sharedPref.edit().putFloat("monthlyBudget", value.toFloat()).apply()

    var lastBudgetAlertShown: Boolean
        get() = sharedPref.getBoolean("lastBudgetAlertShown", false)
        set(value) = sharedPref.edit().putBoolean("lastBudgetAlertShown", value).apply()

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
            .filter { it.date.startsWith(currentMonth) && it.type == "Expense" }
            .sumOf { it.amount }

        val progress = (monthlyExpenses / budget * 100).toInt()

        if ((progress >= 80 || progress >= 100) && !lastBudgetAlertShown) {
            NotificationHelper.showBudgetAlert(context, progress)
            lastBudgetAlertShown = true
        } else if (progress < 80) {
            lastBudgetAlertShown = false
        }
    }

    fun getMonthlyExpenses(): Double {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        return getTransactions()
            .filter { it.date.startsWith(currentMonth) && it.type == "Expense" }
            .sumOf { it.amount }
    }
}