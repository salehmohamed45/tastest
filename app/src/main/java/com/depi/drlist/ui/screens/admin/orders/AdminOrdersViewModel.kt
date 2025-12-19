package com.depi.drlist.ui.screens.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminOrdersUiState {
    object Loading : AdminOrdersUiState()
    data class Success(val orders: List<Order>) : AdminOrdersUiState()
    data class Error(val message: String) : AdminOrdersUiState()
}

class AdminOrdersViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    private val _uiState = MutableStateFlow<AdminOrdersUiState>(AdminOrdersUiState.Loading)
    val uiState: StateFlow<AdminOrdersUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private var allOrders: List<Order> = emptyList()
    
    init {
        loadOrders()
    }
    
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = AdminOrdersUiState.Loading
            
            orderRepository.getAllOrders().fold(
                onSuccess = { orders ->
                    allOrders = orders
                    filterOrders()
                },
                onFailure = { exception ->
                    _uiState.value = AdminOrdersUiState.Error(
                        exception.message ?: "Failed to load orders"
                    )
                }
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterOrders()
    }
    
    fun updateFilter(filter: String) {
        _selectedFilter.value = filter
        filterOrders()
    }
    
    private fun filterOrders() {
        val query = _searchQuery.value
        val filter = _selectedFilter.value
        
        var filteredOrders = allOrders
        
        // Apply status filter
        if (filter != "All") {
            filteredOrders = filteredOrders.filter { it.status == filter }
        }
        
        // Apply search query
        if (query.isNotBlank()) {
            filteredOrders = filteredOrders.filter { order ->
                order.orderId.contains(query, ignoreCase = true) ||
                order.userId.contains(query, ignoreCase = true) ||
                order.status.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.value = AdminOrdersUiState.Success(filteredOrders)
    }
}
