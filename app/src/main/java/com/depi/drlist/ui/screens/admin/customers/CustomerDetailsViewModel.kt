package com.depi.drlist.ui.screens.admin.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.model.User
import com.depi.drlist.data.repository.AuthRepository
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerDetailsData(
    val user: User,
    val orders: List<Order>
)

sealed class CustomerDetailsUiState {
    object Loading : CustomerDetailsUiState()
    data class Success(val data: CustomerDetailsData) : CustomerDetailsUiState()
    data class Error(val message: String) : CustomerDetailsUiState()
}

class CustomerDetailsViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    
    private val _uiState = MutableStateFlow<CustomerDetailsUiState>(CustomerDetailsUiState.Loading)
    val uiState: StateFlow<CustomerDetailsUiState> = _uiState.asStateFlow()
    
    fun loadCustomerDetails(userId: String) {
        viewModelScope.launch {
            _uiState.value = CustomerDetailsUiState.Loading
            
            val userResult = authRepository.getUserById(userId)
            val ordersResult = orderRepository.getAllOrders()
            
            if (userResult.isSuccess && ordersResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val allOrders = ordersResult.getOrNull()!!
                val userOrders = allOrders.filter { it.userId == userId }
                
                _uiState.value = CustomerDetailsUiState.Success(
                    CustomerDetailsData(user = user, orders = userOrders)
                )
            } else {
                _uiState.value = CustomerDetailsUiState.Error(
                    userResult.exceptionOrNull()?.message 
                        ?: ordersResult.exceptionOrNull()?.message 
                        ?: "Failed to load customer details"
                )
            }
        }
    }
}
