package com.example.budgetbee.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbee.models.AppDatabase
import com.example.budgetbee.models.Category
import com.example.budgetbee.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CategoryRepository
    private var currentUserId: String? = null

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            repository.getAllCategories(userId).observeForever { categoryList ->
                _categories.value = categoryList
            }
        }
    }

    fun getCategoriesByType(type: String): LiveData<List<Category>> {
        val userId = currentUserId ?: throw IllegalStateException("User ID not set")
        return repository.getCategoriesByType(userId, type)
    }

    fun createCategory(name: String, iconResId: Int, color: Int, type: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: throw IllegalStateException("User ID not set")
                val category = repository.createCategory(userId, name, iconResId, color, type)
                repository.insertCategory(category)
                loadData()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.updateCategory(category)
                loadData()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                loadData()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 