package com.depi.drlist.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.CartItem
import com.depi.drlist.data.model.Product
import com.depi.drlist.data.repository.CartRepository
import com.depi.drlist.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val products: List<Product>, val cartItemCount: Int) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val cartRepository = CartRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _cartItemCount = MutableStateFlow(0)

    init {
        loadProducts()
        observeCartItems()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                productRepository.getAllProducts().collect { products ->
                    _products.value = products
                    updateUiState()
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load products")
            }
        }
    }

    private fun observeCartItems() {
        viewModelScope.launch {
            try {
                cartRepository.getCartItems().collect { items ->
                    _cartItemCount.value = items.sumOf { it.quantity }
                    updateUiState()
                }
            } catch (e: Exception) {
                // Silently handle cart errors
            }
        }
    }

    private fun updateUiState() {
        _uiState.value = HomeUiState.Success(
            products = _products.value,
            cartItemCount = _cartItemCount.value
        )
    }

    fun addToCart(product: Product, selectedSize: String, quantity: Int = 1) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                product = product,
                quantity = quantity,
                selectedSize = selectedSize,
                totalPrice = product.price * quantity
            )
            cartRepository.addToCart(cartItem)
        }
    }

    fun refresh() {
        loadProducts()
    }
}
