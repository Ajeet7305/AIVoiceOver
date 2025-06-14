package com.aivoiceclassifier.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // Valid credentials for demo purposes
    private val validCredentials = mapOf(
        "admin" to "admin",
        "user" to "password",
        "demo" to "demo",
        "test" to "test123"
    )
    
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            errorMessage = null
        )
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null
        )
    }
    
    fun login() {
        if (_uiState.value.isLoading) return
        
        val currentState = _uiState.value
        
        // Validate input
        if (currentState.username.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Username is required"
            )
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Password is required"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
                isAdminMode = false
            )
            
            // Simulate network delay
            delay(1500)
            
            val isValidLogin = validateCredentials(currentState.username, currentState.password)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccessful = isValidLogin,
                errorMessage = if (!isValidLogin) "Invalid login credentials" else null
            )
        }
    }
    
    fun adminLogin() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isAdminMode = true,
                username = "admin",
                password = "admin"
            )
            
            // Simulate network delay
            delay(1200)
            
            // Admin login always succeeds with preset credentials
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccessful = true,
                errorMessage = null
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun validateCredentials(username: String, password: String): Boolean {
        return validCredentials[username.lowercase().trim()] == password.trim()
    }
    
    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(
            isLoginSuccessful = false,
            errorMessage = null
        )
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isAdminMode: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null
) 