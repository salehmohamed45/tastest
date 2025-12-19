package com.depi.drlist.ui.screens.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val order: Order) : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}

class AdminOrderDetailViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
    
    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = OrderDetailUiState.Loading
            
            orderRepository.getOrderById(orderId).fold(
                onSuccess = { order ->
                    _uiState.value = OrderDetailUiState.Success(order)
                },
                onFailure = { exception ->
                    _uiState.value = OrderDetailUiState.Error(
                        exception.message ?: "Failed to load order"
                    )
                }
            )
        }
    }
    
    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            
            orderRepository.updateOrderStatus(orderId, newStatus).fold(
                onSuccess = {
                    _updateState.value = UpdateState.Success
                    loadOrder(orderId) // Reload order to get updated data
                },
                onFailure = { exception ->
                    _updateState.value = UpdateState.Error(
                        exception.message ?: "Failed to update order status"
                    )
                }
            )
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}
