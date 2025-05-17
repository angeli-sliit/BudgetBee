package com.example.budgetbee.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbee.models.AppDatabase
import com.example.budgetbee.models.User
import com.example.budgetbee.models.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.budgetbee.utils.SharedPrefHelper
import kotlinx.coroutines.flow.firstOrNull

class UserViewModel(application: Application) : AndroidViewModel(application) {
    sealed class AuthResult {
        data class Success(val userId: Long) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    private val repository: UserRepository
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _authResult = MutableLiveData<AuthResult?>()
    val authResult: MutableLiveData<AuthResult?> = _authResult

    private val sharedPrefHelper = SharedPrefHelper(application)

    init {
        try {
            val userDao = AppDatabase.getDatabase(application).userDao()
            repository = UserRepository(userDao)
            Log.d("UserViewModel", "Initialized successfully")
            viewModelScope.launch(Dispatchers.IO) {
                restoreLoggedInUser()
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Failed to initialize database", e)
            throw e
        }
    }

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Attempting to register user: $email")
                
                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    Log.w("UserViewModel", "Registration failed: Empty fields")
                    _authResult.value = AuthResult.Error("All fields are required")
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Log.w("UserViewModel", "Registration failed: Invalid email format")
                    _authResult.value = AuthResult.Error("Invalid email format")
                    return@launch
                }

                if (password.length < 8) {
                    Log.w("UserViewModel", "Registration failed: Password too short")
                    _authResult.value = AuthResult.Error("Password must be at least 8 characters")
                    return@launch
                }

                if (!password.any { it.isDigit() }) {
                    Log.w("UserViewModel", "Registration failed: Password missing number")
                    _authResult.value = AuthResult.Error("Password must contain at least one number")
                    return@launch
                }

                if (!password.any { it.isUpperCase() }) {
                    Log.w("UserViewModel", "Registration failed: Password missing uppercase")
                    _authResult.value = AuthResult.Error("Password must contain at least one uppercase letter")
                    return@launch
                }

                if (!password.any { it.isLowerCase() }) {
                    Log.w("UserViewModel", "Registration failed: Password missing lowercase")
                    _authResult.value = AuthResult.Error("Password must contain at least one lowercase letter")
                    return@launch
                }

                val userId = withContext(Dispatchers.IO) {
                    repository.registerUser(username, email, password)
                }

                if (userId != null && userId > 0) {
                    Log.d("UserViewModel", "Registration successful for user: $email")
                    sharedPrefHelper.loggedInUserId = userId
                    val user = withContext(Dispatchers.IO) { repository.getUserById(userId).firstOrNull() }
                    _currentUser.value = user
                    _authResult.value = AuthResult.Success(userId)
                } else if (userId == -1L) {
                    Log.w("UserViewModel", "Registration failed: User already exists")
                    _authResult.value = AuthResult.Error("Email already registered")
                } else {
                    Log.e("UserViewModel", "Registration failed: Unknown error")
                    _authResult.value = AuthResult.Error("Registration failed")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Registration error", e)
                _authResult.value = AuthResult.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Attempting to login user: $email")
                
                if (email.isBlank() || password.isBlank()) {
                    Log.w("UserViewModel", "Login failed: Empty fields")
                    _authResult.value = AuthResult.Error("Email and password are required")
                    return@launch
                }

                val user = withContext(Dispatchers.IO) {
                    repository.loginUser(email, password)
                }

                if (user != null) {
                    Log.d("UserViewModel", "Login successful for user: $email")
                    withContext(Dispatchers.IO) {
                        sharedPrefHelper.loggedInUserId = user.id
                    }
                    _currentUser.value = user
                    _authResult.value = AuthResult.Success(user.id)
                } else {
                    Log.w("UserViewModel", "Login failed: Invalid credentials")
                    _authResult.value = AuthResult.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Login error", e)
                _authResult.value = AuthResult.Error("Login failed: ${e.message}")
            }
        }
    }

    fun logout() {
        Log.d("UserViewModel", "Logging out user")
        sharedPrefHelper.clearLoggedInUser()
        _currentUser.value = null
        _authResult.value = null
    }

    private fun restoreLoggedInUser() {
        val userId = sharedPrefHelper.loggedInUserId
        if (userId > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                val user = repository.getUserById(userId).firstOrNull()
                withContext(Dispatchers.Main) {
                    _currentUser.value = user
                }
            }
        }
    }

    fun updateCurrency(userId: Long, currency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCurrency(userId, currency)
            // Optionally refresh user data
            val user = repository.getUserById(userId).firstOrNull()
            _currentUser.postValue(user)
        }
    }
} 
