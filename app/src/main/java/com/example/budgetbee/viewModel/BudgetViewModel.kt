package com.example.budgetbee.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbee.models.AppDatabase
import com.example.budgetbee.models.Budget
import com.example.budgetbee.repository.BudgetRepository
import com.example.budgetbee.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BudgetRepository
    private var currentUserId: String? = null
    private var lastBudgetAlertShown = false
    private var lastBudgetWarningPercentage = 0

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    private val _totalBudget = MutableLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    private val _budgetProgress = MutableLiveData<Pair<Int, String>>()
    val budgetProgress: LiveData<Pair<Int, String>> = _budgetProgress

    init {
        val budgetDao = AppDatabase.getDatabase(application).budgetDao()
        repository = BudgetRepository(budgetDao)
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            
            // Load budgets for current month
            repository.getBudgetsByMonth(userId, currentMonth).observeForever { budgetList ->
                _budgets.value = budgetList
            }

            // Load total budget for current month
            repository.getTotalBudgetForMonth(userId, currentMonth).observeForever { total ->
                _totalBudget.value = total
            }
        }
    }

    fun createBudget(amount: Double, category: String) {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            val budget = repository.createBudget(userId, amount, category)
            repository.insertBudget(budget)
            loadData()
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            repository.updateBudget(budget)
            loadData()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            loadData()
        }
    }

    fun updateBudgetProgress(expenses: Double) {
        val total = _totalBudget.value ?: 0.0
        if (total <= 0) {
            _budgetProgress.value = Pair(0, "Budget not set")
            return
        }

        val progress = (expenses / total * 100).toInt().coerceIn(0, 100)
        val message = "$progress% of budget used"
        _budgetProgress.value = Pair(progress, message)

        // Handle notifications based on progress
        when {
            progress >= 100 -> {
                if (!lastBudgetAlertShown) {
                    NotificationHelper.showBudgetAlert(getApplication(), progress)
                    lastBudgetAlertShown = true
                    lastBudgetWarningPercentage = progress
                }
            }
            progress in 80..99 -> {
                if (lastBudgetWarningPercentage < 80 || progress > lastBudgetWarningPercentage) {
                    NotificationHelper.showBudgetWarning(getApplication(), progress)
                    lastBudgetWarningPercentage = progress
                    lastBudgetAlertShown = false
                }
            }
            else -> {
                // Reset states when below thresholds
                if (lastBudgetAlertShown || lastBudgetWarningPercentage > 0) {
                    lastBudgetAlertShown = false
                    lastBudgetWarningPercentage = 0
                }
            }
        }
    }
}
