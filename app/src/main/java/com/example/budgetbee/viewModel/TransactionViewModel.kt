package com.example.budgetbee.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionType
import com.example.budgetbee.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _transactions.value = transactions
                updateTotals(transactions)
            }
        }
    }

    private fun updateTotals(transactions: List<Transaction>) {
        _totalIncome.value = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        _totalExpense.value = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun getTransactionsByType(type: TransactionType) {
        viewModelScope.launch {
            repository.getTransactionsByType(type).collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    fun getTransactionsByCategory(category: String) {
        viewModelScope.launch {
            repository.getTransactionsByCategory(category).collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            repository.getTransactionsBetweenDates(startDate, endDate).collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    fun loadCategories(type: TransactionType) {
        viewModelScope.launch {
            repository.getCategoriesByType(type).collect { categories ->
                _categories.value = categories
            }
        }
    }
} 