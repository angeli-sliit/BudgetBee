package com.example.budgetbee.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.budgetbee.activities.MainActivity
import java.util.Calendar

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefHelper = SharedPrefHelper(context)
        if (sharedPrefHelper.dailyReminderEnabled) {
            // Start the alarm activity
            val alarmIntent = Intent(context, com.example.budgetbee.activities.AlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(alarmIntent)
            
            // Set the next alarm
            setNextAlarm(context)
        }
    }

    companion object {
        private const val TAG = "DailyReminderReceiver"
        private const val ALARM_HOUR = 20
        private const val ALARM_MINUTE = 0

        fun setNextAlarm(context: Context) {
            val sharedPrefHelper = SharedPrefHelper(context)
            if (!sharedPrefHelper.dailyReminderEnabled) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
                return
            }

            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = "com.example.budgetbee.DAILY_REMINDER"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Set alarm for specified time
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, ALARM_HOUR)
                set(Calendar.MINUTE, ALARM_MINUTE)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If time already passed, add 1 day
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Daily reminder alarm set for ${calendar.time}")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to set exact alarm: ${e.message}")
            }
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = "com.example.budgetbee.DAILY_REMINDER"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Daily reminder alarm canceled")
        }
    }
}
