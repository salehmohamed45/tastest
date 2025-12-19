package com.depi.drlist.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.repository.OrderRepository
import com.depi.drlist.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val recentOrders: List<Order> = emptyList()
)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val stats: DashboardStats) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class AdminDashboardViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            orderRepository.getAllOrders().fold(
                onSuccess = { orders ->
                    val pendingOrders = orders.count { it.status == "Pending" }
                    val completedOrders = orders.count { 
                        it.status == "Delivered" || it.status == "Completed" 
                    }
                    val totalRevenue = orders.filter { 
                        it.status == "Delivered" || it.status == "Completed" 
                    }.sumOf { it.totalAmount }
                    
                    val stats = DashboardStats(
                        totalOrders = orders.size,
                        pendingOrders = pendingOrders,
                        completedOrders = completedOrders,
                        totalRevenue = totalRevenue,
                        recentOrders = orders.take(5)
                    )
                    _uiState.value = DashboardUiState.Success(stats)
                },
                onFailure = { exception ->
                    _uiState.value = DashboardUiState.Error(
                        exception.message ?: "Failed to load dashboard data"
                    )
                }
            )
        }
    }
}
