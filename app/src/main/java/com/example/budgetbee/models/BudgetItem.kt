package com.example.budgetbee.models

data class BudgetItem(
    val category: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val currency: String
) {
    val progress: Int
        get() = if (budgetAmount > 0) ((spentAmount / budgetAmount) * 100).toInt().coerceIn(0, 100) else 0
} 