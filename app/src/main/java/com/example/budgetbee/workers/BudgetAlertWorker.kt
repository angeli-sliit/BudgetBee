package com.example.budgetbee.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.budgetbee.repository.TransactionRepository
import com.example.budgetbee.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.time

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfMonth = calendar.time

            val totalExpenses = repository.getTotalAmountByTypeAndDateRange(
                com.example.budgetbee.models.TransactionType.EXPENSE,
                startOfMonth,
                endOfMonth
            )

            val prefs = applicationContext.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
            val monthlyBudget = prefs.getFloat("monthly_budget", 0f)

            if (monthlyBudget > 0) {
                val progress = ((totalExpenses / monthlyBudget) * 100).toInt()
                notificationHelper.checkAndSendBudgetNotification(progress)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 