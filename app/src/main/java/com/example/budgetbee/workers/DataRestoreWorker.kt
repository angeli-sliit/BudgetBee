package com.example.budgetbee.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionType
import com.example.budgetbee.repository.TransactionRepository
import com.example.budgetbee.utils.NotificationHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class DataRestoreWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val backupDir = File(applicationContext.filesDir, "backups")
            if (!backupDir.exists() || !backupDir.isDirectory) {
                notificationHelper.showImportFailure("No backup directory found")
                return Result.failure()
            }

            val backupFiles = backupDir.listFiles { file -> 
                file.name.startsWith("backup_") && file.name.endsWith(".json") 
            }?.sortedByDescending { it.lastModified() }

            if (backupFiles.isNullOrEmpty()) {
                notificationHelper.showImportFailure("No backup files found")
                return Result.failure()
            }

            val latestBackup = backupFiles.first()
            val json = latestBackup.readText()
            
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactions = Gson().fromJson<List<Transaction>>(json, type)

            if (transactions.isNullOrEmpty()) {
                notificationHelper.showImportFailure("No transactions found in backup")
                return Result.failure()
            }

            repository.insertAll(transactions)
            notificationHelper.showImportSuccess(transactions.size)
            Result.success()
        } catch (e: Exception) {
            notificationHelper.showImportFailure(e.message ?: "Unknown error during restore")
            Result.failure()
        }
    }
} 