package com.example.budgetbee.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId")
    fun getTransactionsByUser(userId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type")
    fun getTransactionsByType(userId: Long, type: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date LIKE :month || '%'")
    fun getTransactionsByMonth(userId: Long, month: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date LIKE :month || '%'")
    suspend fun getTransactionsByMonthOnce(userId: Long, month: String): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactions(userId: Long)
} 
