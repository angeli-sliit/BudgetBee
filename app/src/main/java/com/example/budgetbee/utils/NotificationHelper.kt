package com.example.budgetbee.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.budgetbee.R
import com.example.budgetbee.activities.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "budget_alertsv1"
    private const val IMPORT_CHANNEL_ID = "import_exportv1"
    private const val TRANSACTION_CHANNEL_ID = "transaction_updatesv1"
    private const val BUDGET_ALERT_ID = 1001
    private const val BUDGET_WARNING_ID = 1002
    private const val IMPORT_SUCCESS_ID = 1003
    private const val IMPORT_FAILURE_ID = 1004
    private const val TRANSACTION_SUCCESS_ID = 1005

    private val VIBRATION_PATTERN = longArrayOf(0, 500, 200, 500)

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Budget Alerts Channel
            val budgetChannel = NotificationChannel(
                CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Critical budget warnings and alerts"
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setSound(null, null)
                setShowBadge(true)
            }

            // Import/Export Channel
            val importChannel = NotificationChannel(
                IMPORT_CHANNEL_ID,
                "Import/Export",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Import and export notifications"
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setSound(null, null)
                setShowBadge(true)
            }

            // Transaction Updates Channel
            val transactionChannel = NotificationChannel(
                TRANSACTION_CHANNEL_ID,
                "Transaction Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Transaction save and update notifications"
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setSound(null, null)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(budgetChannel, importChannel, transactionChannel))
        }
    }

    private fun createBaseNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(null)
            .setVibrate(VIBRATION_PATTERN)
    }

    fun showBudgetAlert(context: Context, progress: Int) {
        val notification = createBaseNotification(
            context,
            CHANNEL_ID,
            context.getString(R.string.budget_exceeded),
            "You've spent $progress% of your budget!",
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(BUDGET_ALERT_ID, notification)
    }

    fun showBudgetWarning(context: Context, progress: Int) {
        val notification = createBaseNotification(
            context,
            CHANNEL_ID,
            context.getString(R.string.budget_warning),
            "You've used $progress% of your budget!",
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(BUDGET_WARNING_ID, notification)
    }

    fun showImportSuccess(context: Context, count: Int) {
        val notification = createBaseNotification(
            context,
            IMPORT_CHANNEL_ID,
            context.getString(R.string.import_success),
            "$count ${context.getString(R.string.transactions_imported)}",
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(IMPORT_SUCCESS_ID, notification)
    }

    fun showImportFailure(context: Context, error: String? = null) {
        val notification = createBaseNotification(
            context,
            IMPORT_CHANNEL_ID,
            context.getString(R.string.import_failed),
            error ?: context.getString(R.string.default_import_error),
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(IMPORT_FAILURE_ID, notification)
    }

    fun showBudgetUpdateNotification(context: Context, title: String, message: String) {
        val notification = createBaseNotification(
            context,
            CHANNEL_ID,
            title,
            message,
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(BUDGET_ALERT_ID, notification)
    }

    fun showExportSuccess(context: Context, operation: String) {
        val notification = createBaseNotification(
            context,
            IMPORT_CHANNEL_ID,
            context.getString(R.string.export_success),
            "$operation completed successfully",
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(IMPORT_SUCCESS_ID, notification)
    }

    fun showExportFailure(context: Context, operation: String) {
        val notification = createBaseNotification(
            context,
            IMPORT_CHANNEL_ID,
            context.getString(R.string.export_failed),
            "$operation failed",
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(IMPORT_FAILURE_ID, notification)
    }

    fun showDailyReminder(context: Context) {
        val notification = createBaseNotification(
            context,
            CHANNEL_ID,
            context.getString(R.string.daily_reminder),
            "Don't forget to track your expenses today!",
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(BUDGET_WARNING_ID, notification)
    }

    fun showTransactionSuccess(context: Context, message: String) {
        val notification = createBaseNotification(
            context,
            TRANSACTION_CHANNEL_ID,
            "Transaction Saved",
            message,
            NotificationCompat.PRIORITY_DEFAULT
        ).build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(TRANSACTION_SUCCESS_ID, notification)
    }
}
