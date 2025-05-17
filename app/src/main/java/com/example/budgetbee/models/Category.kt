package com.example.budgetbee.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val iconResId: Int,
    val color: Int,
    val type: String, // "Income" or "Expense"
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)