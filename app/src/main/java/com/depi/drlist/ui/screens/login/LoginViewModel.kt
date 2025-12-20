package com.depi.drlist.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.User
import com.depi.drlist.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

// ✅ NEW: Loading state for initial auth check
sealed class AuthCheckState {
    data object Checking : AuthCheckState()
    data object Completed : AuthCheckState()
}

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // ✅ NEW: Track initial auth check
    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState = _authCheckState.asStateFlow()

    init {
        // ✅ Check auth immediately on ViewModel creation
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
            _authCheckState.value = AuthCheckState.Completed
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signUp(name, email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please provide email and password")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signIn(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}