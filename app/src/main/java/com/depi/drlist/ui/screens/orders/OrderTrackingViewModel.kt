package com.depi.drlist.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderTrackingUiState {
    object Loading : OrderTrackingUiState()
    data class Success(val order: Order) : OrderTrackingUiState()
    data class Error(val message: String) : OrderTrackingUiState()
}

class OrderTrackingViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    private val _uiState = MutableStateFlow<OrderTrackingUiState>(OrderTrackingUiState.Loading)
    val uiState: StateFlow<OrderTrackingUiState> = _uiState.asStateFlow()
    
    private val _isAutoRefreshing = MutableStateFlow(false)
    val isAutoRefreshing: StateFlow<Boolean> = _isAutoRefreshing.asStateFlow()
    
    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = OrderTrackingUiState.Loading
            
            orderRepository.getOrderById(orderId).fold(
                onSuccess = { order ->
                    _uiState.value = OrderTrackingUiState.Success(order)
                },
                onFailure = { exception ->
                    _uiState.value = OrderTrackingUiState.Error(
                        exception.message ?: "Failed to load order"
                    )
                }
            )
        }
    }
    
    fun startAutoRefresh(orderId: String) {
        if (_isAutoRefreshing.value) return
        
        _isAutoRefreshing.value = true
        viewModelScope.launch {
            while (_isAutoRefreshing.value) {
                delay(30000) // Refresh every 30 seconds
                orderRepository.getOrderById(orderId).fold(
                    onSuccess = { order ->
                        _uiState.value = OrderTrackingUiState.Success(order)
                    },
                    onFailure = { /* Silent failure for auto-refresh */ }
                )
            }
        }
    }
    
    fun stopAutoRefresh() {
        _isAutoRefreshing.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
