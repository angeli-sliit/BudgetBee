package com.example.budgetbee.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis()
) 
