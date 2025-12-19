package com.depi.drlist.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.model.User
import com.depi.drlist.data.repository.AuthRepository
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User, val orders: List<Order>) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    orderRepository.getUserOrders()
                        .onSuccess { orders ->
                            _uiState.value = ProfileUiState.Success(user, orders)
                        }
                        .onFailure {
                            _uiState.value = ProfileUiState.Success(user, emptyList())
                        }
                } else {
                    _uiState.value = ProfileUiState.Error("User not found")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.updateUserProfile(user)
                .onSuccess {
                    loadProfile()
                    onSuccess()
                }
                .onFailure { exception ->
                    onError(exception.message ?: "Failed to update profile")
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
