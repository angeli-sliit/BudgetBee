package com.example.budgetbee.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.budgetbee.R
import com.example.budgetbee.activities.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "budget_bee_channel"
    private const val BUDGET_ALERT_ID = 1001
    private const val DAILY_REMINDER_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Budget alerts and reminders"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetAlert(context: Context, progress: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (progress >= 100) {
            context.getString(R.string.budget_exceeded)
        } else {
            context.getString(R.string.budget_warning)
        }

        val message = if (progress >= 100) {
            "You've spent $progress% of your budget!"
        } else {
            "You've spent $progress% of your budget"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(BUDGET_ALERT_ID, notification)
    }

    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(DAILY_REMINDER_ID, notification)
    }

    // NotificationHelper.kt
    fun checkAndSendBudgetNotification(context: Context) {
        val sharedPref = SharedPrefHelper(context)
        val budget = sharedPref.monthlyBudget
        val expenses = sharedPref.getMonthlyExpenses()

        if (budget <= 0) return

        val progress = (expenses / budget * 100).toInt()
        when {
            progress >= 100 -> showBudgetAlert(context, progress)
            progress >= 80 -> showBudgetAlert(context, progress)
        }
    }
}