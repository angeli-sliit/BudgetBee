package com.example.budgetbee.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getAllCategories(userId: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type")
    fun getCategoriesByType(userId: String, type: String): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategories(userId: String)
} 