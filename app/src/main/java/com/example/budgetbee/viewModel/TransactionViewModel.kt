package com.example.budgetbee.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbee.models.AppDatabase
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private var currentUserId: Long? = null
    private val TAG = "TransactionViewModel"

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
    }

    fun setCurrentUserId(userId: Long) {
        currentUserId = userId
        Log.d(TAG, "Current user ID set to: $userId")
    }

    fun getTransactions(): LiveData<List<Transaction>> {
        val userId = currentUserId ?: throw IllegalStateException("User ID not set")
        Log.d(TAG, "Getting transactions for user: $userId")
        return repository.getTransactionsByUser(userId)
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        val userId = currentUserId ?: throw IllegalStateException("User ID not set")
        Log.d(TAG, "Getting transactions of type $type for user: $userId")
        return repository.getTransactionsByType(userId, type)
    }

    fun getTransactionsByMonth(month: String): LiveData<List<Transaction>> {
        val userId = currentUserId ?: throw IllegalStateException("User ID not set")
        Log.d(TAG, "Getting transactions for month $month, user: $userId")
        return repository.getTransactionsByMonth(userId, month)
    }

    suspend fun saveTransaction(transaction: Transaction) {
        try {
            Log.d(TAG, "Saving transaction: ${transaction.id}")
            repository.insertTransaction(transaction)
            Log.d(TAG, "Transaction saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transaction: ${e.message}", e)
            throw e
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating transaction: ${transaction.id}")
                repository.updateTransaction(transaction)
                Log.d(TAG, "Transaction updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating transaction: ${e.message}", e)
                throw e
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting transaction: ${transaction.id}")
                repository.deleteTransaction(transaction)
                Log.d(TAG, "Transaction deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting transaction: ${e.message}", e)
                throw e
            }
        }
    }

    fun createTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: String,
        note: String = ""
    ): Transaction {
        val userId = currentUserId ?: throw IllegalStateException("User ID not set")
        Log.d(TAG, "Creating new transaction for user: $userId")
        return repository.createTransaction(
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
        return repository.getTransactionsByMonthOnce(userId, month)
    }
} 
