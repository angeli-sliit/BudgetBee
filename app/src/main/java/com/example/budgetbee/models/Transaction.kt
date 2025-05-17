package com.example.budgetbee.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String,
    val userId: Long,
    val title: String,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val category: String,
    val date: String,
    val note: String = ""
) : Parcelable