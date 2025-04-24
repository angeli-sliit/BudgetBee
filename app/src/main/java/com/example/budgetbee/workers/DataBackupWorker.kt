package com.example.budgetbee.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.budgetbee.repository.TransactionRepository
import com.example.budgetbee.utils.NotificationHelper
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class DataBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val transactions = repository.getAllTransactions()
            val gson = Gson()
            val json = gson.toJson(transactions)

            val backupDir = File(applicationContext.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.json")
            backupFile.writeText(json)

            notificationHelper.showExportSuccess("Export")
            Result.success()
        } catch (e: Exception) {
            notificationHelper.showExportFailure(e.message ?: "Unknown error")
            Result.failure()
        }
    }
} 