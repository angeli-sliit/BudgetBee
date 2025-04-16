package com.example.budgetbee.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

// models/Transaction.kt
@Parcelize
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val category: String,
    val date: String,
    val note: String = ""
) : Parcelable