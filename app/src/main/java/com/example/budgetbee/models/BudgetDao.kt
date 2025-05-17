package com.example.budgetbee.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month")
    fun getBudgetsByMonth(userId: String, month: String): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category")
    fun getBudgetsByCategory(userId: String, category: String): LiveData<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllBudgets(userId: String)

    @Query("SELECT SUM(amount) FROM budgets WHERE userId = :userId AND month = :month")
    fun getTotalBudgetForMonth(userId: String, month: String): LiveData<Double>
} 