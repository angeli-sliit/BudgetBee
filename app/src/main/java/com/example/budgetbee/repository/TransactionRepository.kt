package com.example.budgetbee.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionDao
import java.util.UUID

class TransactionRepository(private val transactionDao: TransactionDao) {
    private val TAG = "TransactionRepository"

    fun getTransactionsByUser(userId: Long): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userId)
    }

    fun getTransactionsByType(userId: Long, type: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(userId, type)
    }

    fun getTransactionsByMonth(userId: Long, month: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByMonth(userId, month)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        try {
            Log.d(TAG, "Inserting transaction: ${transaction.id}")
            transactionDao.insertTransaction(transaction)
            Log.d(TAG, "Transaction inserted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting transaction: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        try {
            Log.d(TAG, "Updating transaction: ${transaction.id}")
            transactionDao.updateTransaction(transaction)
            Log.d(TAG, "Transaction updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        try {
            Log.d(TAG, "Deleting transaction: ${transaction.id}")
            transactionDao.deleteTransaction(transaction)
            Log.d(TAG, "Transaction deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAllTransactions(userId: Long) {
        transactionDao.deleteAllTransactions(userId)
    }

    fun createTransaction(
        userId: Long,
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: String,
        note: String = ""
    ): Transaction {
        Log.d(TAG, "Creating new transaction for user: $userId")
        return Transaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            amount = amount,
            type = type,
            category = category,
            date = date,
            note = note
        )
    }

    suspend fun getTransactionsByMonthOnce(userId: Long, month: String): List<Transaction> {
        return transactionDao.getTransactionsByMonthOnce(userId, month)
    }
} 
