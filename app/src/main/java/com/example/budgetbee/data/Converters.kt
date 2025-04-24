package com.example.budgetbee.data

import androidx.room.TypeConverter
import com.example.budgetbee.models.RecurringPeriod
import com.example.budgetbee.models.TransactionType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromRecurringPeriod(value: RecurringPeriod?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRecurringPeriod(value: String?): RecurringPeriod? {
        return value?.let { RecurringPeriod.valueOf(it) }
    }
} 