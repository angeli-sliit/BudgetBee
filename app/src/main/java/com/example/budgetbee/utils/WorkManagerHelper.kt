package com.example.budgetbee.utils

import android.content.Context
import androidx.work.*
import com.example.budgetbee.workers.BudgetAlertWorker
import com.example.budgetbee.workers.DailyReminderWorker
import com.example.budgetbee.workers.DataBackupWorker
import com.example.budgetbee.workers.DataRestoreWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    fun scheduleDailyReminder() {
        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )
    }

    fun scheduleBudgetAlert() {
        val budgetAlertRequest = PeriodicWorkRequestBuilder<BudgetAlertWorker>(
            12, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "budget_alert",
            ExistingPeriodicWorkPolicy.KEEP,
            budgetAlertRequest
        )
    }

    fun scheduleDataBackup() {
        val dataBackupRequest = PeriodicWorkRequestBuilder<DataBackupWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "data_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            dataBackupRequest
        )
    }

    fun performDataBackup() {
        val dataBackupRequest = OneTimeWorkRequestBuilder<DataBackupWorker>()
            .build()

        workManager.enqueue(dataBackupRequest)
    }

    fun performDataRestore() {
        val dataRestoreRequest = OneTimeWorkRequestBuilder<DataRestoreWorker>()
            .build()

        workManager.enqueue(dataRestoreRequest)
    }
} 