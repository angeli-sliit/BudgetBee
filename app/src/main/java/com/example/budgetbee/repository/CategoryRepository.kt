package com.example.budgetbee.repository

import androidx.lifecycle.LiveData
import com.example.budgetbee.models.Category
import com.example.budgetbee.models.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAllCategories(userId: String): LiveData<List<Category>> {
        return categoryDao.getAllCategories(userId)
    }

    fun getCategoriesByType(userId: String, type: String): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(userId, type)
    }

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteAllCategories(userId: String) {
        categoryDao.deleteAllCategories(userId)
    }

    fun createCategory(
        userId: String,
        name: String,
        iconResId: Int,
        color: Int,
        type: String
    ): Category {
        return Category(
            userId = userId,
            name = name,
            iconResId = iconResId,
            color = color,
            type = type
        )
    }
} 