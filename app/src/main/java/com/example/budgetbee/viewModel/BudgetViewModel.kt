package com.example.budgetbee.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.utils.SharedPrefHelper
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    val sharedPrefHelper = SharedPrefHelper(application) // Changed from private to public

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _budgetProgress = MutableLiveData<Pair<Int, String>>()
    val budgetProgress: LiveData<Pair<Int, String>> = _budgetProgress

    fun loadData() {
        viewModelScope.launch {
            val currentTransactions = sharedPrefHelper.getTransactions()
            _transactions.postValue(currentTransactions)

            val budget = sharedPrefHelper.monthlyBudget
            val expenses = currentTransactions
                .filter { it.type == "Expense" }
                .sumOf { it.amount }

            val progress = if (budget > 0) (expenses / budget * 100).toInt() else 0
            val message = if (budget > 0) "$progress% of budget used" else "Budget not set"
            _budgetProgress.postValue(Pair(progress, message))
        }
    }
}