package com.example.budgetbee.repository

import androidx.lifecycle.LiveData
import com.example.budgetbee.models.Budget
import com.example.budgetbee.models.BudgetDao
import java.text.SimpleDateFormat
import java.util.*

class BudgetRepository(private val budgetDao: BudgetDao) {
    fun getBudgetsByMonth(userId: String, month: String): LiveData<List<Budget>> {
        return budgetDao.getBudgetsByMonth(userId, month)
    }

    fun getBudgetsByCategory(userId: String, category: String): LiveData<List<Budget>> {
        return budgetDao.getBudgetsByCategory(userId, category)
    }

    suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteAllBudgets(userId: String) {
        budgetDao.deleteAllBudgets(userId)
    }

    fun getTotalBudgetForMonth(userId: String, month: String): LiveData<Double> {
        return budgetDao.getTotalBudgetForMonth(userId, month)
    }

    fun createBudget(
        userId: String,
        amount: Double,
        category: String,
        month: String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    ): Budget {
        return Budget(
            userId = userId,
            amount = amount,
            month = month,
            category = category
        )
    }
} 