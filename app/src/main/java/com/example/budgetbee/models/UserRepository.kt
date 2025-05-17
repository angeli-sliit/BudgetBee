package com.example.budgetbee.models

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun registerUser(username: String, email: String, password: String): Long? {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) return -1L // Email already exists
        val user = User(username = username, email = email, password = password)
        return userDao.insertUser(user)
    }

    suspend fun loginUser(email: String, password: String): User? {
        return userDao.loginUser(email, password)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    fun getUserById(userId: Long): Flow<User> {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun updateCurrency(userId: Long, currency: String) {
        userDao.updateCurrency(userId, currency)
    }
} 
