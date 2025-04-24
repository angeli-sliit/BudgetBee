package com.example.budgetbee.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

// models/Transaction.kt
@Entity(tableName = "transactions")
@Parcelize
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType? = null,
    val description: String? = null,
    val receiptImagePath: String? = null,
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (id != other.id) return false
        if (title != other.title) return false
        if (amount != other.amount) return false
        if (category != other.category) return false
        if (date != other.date) return false
        if (type != other.type) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class RecurringPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}