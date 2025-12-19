package com.depi.drlist.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderHistoryUiState {
    object Loading : OrderHistoryUiState()
    data class Success(val orders: List<Order>) : OrderHistoryUiState()
    data class Error(val message: String) : OrderHistoryUiState()
}

class OrderHistoryViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadOrders()
    }
    
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrderHistoryUiState.Loading
            
            orderRepository.getUserOrders().fold(
                onSuccess = { orders ->
                    _uiState.value = OrderHistoryUiState.Success(orders)
                },
                onFailure = { exception ->
                    _uiState.value = OrderHistoryUiState.Error(
                        exception.message ?: "Failed to load orders"
                    )
                }
            )
        }
    }
}
