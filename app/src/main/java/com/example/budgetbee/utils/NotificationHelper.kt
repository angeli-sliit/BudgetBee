package com.example.budgetbee.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.budgetbee.R
import com.example.budgetbee.activities.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "budget_bee_channel"
    private const val REMINDER_CHANNEL_ID = "reminder_channel"
    private const val BUDGET_ALERT_ID = 1001
    private const val DAILY_REMINDER_ID = 1002
    private const val BUDGET_WARNING_ID = 1003
    private const val EXPORT_CHANNEL_ID = "export_channel"
    private const val EXPORT_SUCCESS_ID = 1004
    private const val BUDGET_UPDATE_ID = 1005

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Budget Alerts Channel
            val budgetChannel = NotificationChannel(
                CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical budget warnings and alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableLights(true)
                lightColor = Color.RED
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
// Export Channel
            val exportChannel = NotificationChannel(
                EXPORT_CHANNEL_ID,
                "Exports",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Export status notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(500, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                enableLights(true)
                lightColor = Color.BLUE
            }
            // Reminders Channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                enableLights(true)
                lightColor = Color.GREEN
            }


            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(exportChannel)
        }
    }

    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setLights(Color.GREEN, 1000, 1000)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(DAILY_REMINDER_ID, notification)
    }

    fun showBudgetAlert(context: Context, progress: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setLights(Color.RED, 1000, 1000)
            .setTimeoutAfter(60000)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(BUDGET_ALERT_ID, notification)
    }

    fun checkAndSendBudgetNotification(context: Context, progress: Int) {
        val prefs = context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
        val lastNotified = prefs.getInt("lastBudgetNotif", 0)

        when {
            progress >= 100 && lastNotified < 100 -> {
                showBudgetAlert(context, progress)
                prefs.edit().putInt("lastBudgetNotif", 100).apply()
            }
            progress >= 80 && lastNotified < 80 -> {
                showBudgetAlert(context, progress)
                prefs.edit().putInt("lastBudgetNotif", 80).apply()
            }
            progress >= 50 && lastNotified < 50 -> {
                showBudgetWarning(context, progress)
                prefs.edit().putInt("lastBudgetNotif", 50).apply()
            }
            progress < 50 -> prefs.edit().putInt("lastBudgetNotif", 0).apply()
        }
    }
    fun showExportSuccess(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, EXPORT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Export Successful")
            .setContentText("PDF report saved to Downloads")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(EXPORT_SUCCESS_ID, notification)
    }

    fun showExportFailure(context: Context) {
        val notification = NotificationCompat.Builder(context, EXPORT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Export Failed")
            .setContentText("Failed to generate PDF report")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(EXPORT_SUCCESS_ID, notification)
    }
    private fun showBudgetWarning(context: Context, progress: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Warning")
            .setContentText("You've used $progress% of budget!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000))
            .setLights(Color.RED, 1000, 1000)
            .setTimeoutAfter(60000)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(BUDGET_WARNING_ID, notification)
    }
    fun showBudgetUpdateNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(500, 500))
            .setLights(Color.GREEN, 1000, 1000)
            .setTimeoutAfter(60000)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(BUDGET_UPDATE_ID, notification)
    }
}