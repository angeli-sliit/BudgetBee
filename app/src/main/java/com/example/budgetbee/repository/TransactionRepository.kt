package com.example.budgetbee.repository

import com.example.budgetbee.data.TransactionDao
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(category)

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetweenDates(startDate, endDate)

    suspend fun getTotalAmountByTypeAndDateRange(type: TransactionType, startDate: Date, endDate: Date): Double =
        transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate) ?: 0.0

    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    fun getCategoriesByType(type: TransactionType): Flow<List<String>> = transactionDao.getCategoriesByType(type)

    suspend fun insertAll(transactions: List<Transaction>) {
        transactions.forEach { transaction ->
            transactionDao.insertTransaction(transaction)
        }
    }
} 