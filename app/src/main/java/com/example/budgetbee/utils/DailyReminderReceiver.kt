package com.example.budgetbee.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val sharedPrefHelper = SharedPrefHelper(context)

        // Only show if enabled in settings
        if (!sharedPrefHelper.dailyReminderEnabled) return

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hasTodayExpenses = sharedPrefHelper.getTransactions()
            .any { it.date == today && it.type == "Expense" }

        if (!hasTodayExpenses) {
            NotificationHelper.showDailyReminder(context)
        }
    }
}