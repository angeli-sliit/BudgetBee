package com.example.budgetbee.utils

import android.app.AlarmManager
import android.content.Context
import android.os.Build


object AlarmPermissionHelper {
    private const val TAG = "AlarmPermissionHelper"

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

}