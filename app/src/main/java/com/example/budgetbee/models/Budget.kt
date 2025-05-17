package com.example.budgetbee.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets", indices = [androidx.room.Index(value = ["userId", "month", "category"], unique = true)])
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val amount: Double,
    val month: String, // Format: "YYYY-MM"
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
) 
