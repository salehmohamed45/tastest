package com.depi.drlist.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.CartItem
import com.depi.drlist.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CartUiState {
    object Loading : CartUiState()
    data class Success(val items: List<CartItem>, val totalPrice: Double) : CartUiState()
    data class Error(val message: String) : CartUiState()
}

class CartViewModel : ViewModel() {
    private val cartRepository = CartRepository()

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            try {
                cartRepository.getCartItems().collect { items ->
                    val totalPrice = items.sumOf { it.calculateTotalPrice() }
                    _uiState.value = CartUiState.Success(items, totalPrice)
                }
            } catch (e: Exception) {
                _uiState.value = CartUiState.Error(e.message ?: "Failed to load cart")
            }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            cartRepository.updateCartItemQuantity(productId, newQuantity)
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}
