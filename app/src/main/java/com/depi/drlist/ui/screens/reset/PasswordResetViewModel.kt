package com.depi.drlist.ui.screens.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PasswordResetState {
    data object Idle : PasswordResetState()
    data object Loading : PasswordResetState()
    data object Success : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

class PasswordResetViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _resetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val resetState = _resetState.asStateFlow()

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _resetState.value = PasswordResetState.Error("Please enter your email address")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetState.value = PasswordResetState.Error("Please enter a valid email address")
            return
        }

        _resetState.value = PasswordResetState.Loading
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _resetState.value = PasswordResetState.Success
                }
                .onFailure { exception ->
                    _resetState.value = PasswordResetState.Error(
                        exception.message ?: "Failed to send reset email"
                    )
                }
        }
    }

    fun resetState() {
        _resetState.value = PasswordResetState.Idle
    }
}
