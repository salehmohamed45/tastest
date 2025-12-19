package com.depi.drlist.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.CartItem
import com.depi.drlist.data.model.Product
import com.depi.drlist.data.repository.CartRepository
import com.depi.drlist.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductDetailUiState {
    data object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

sealed class AddToCartState {
    data object Idle : AddToCartState()
    data object Loading : AddToCartState()
    data object Success : AddToCartState()
    data class Error(val message: String) : AddToCartState()
}

class ProductDetailViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val cartRepository = CartRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartState>(AddToCartState.Idle)
    val addToCartState = _addToCartState.asStateFlow()

    fun loadProduct(productId: String) {
        _uiState.value = ProductDetailUiState.Loading
        viewModelScope.launch {
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    _uiState.value = ProductDetailUiState.Success(product)
                }
                .onFailure { exception ->
                    _uiState.value = ProductDetailUiState.Error(
                        exception.message ?: "Failed to load product"
                    )
                }
        }
    }

    fun addToCart(product: Product, selectedSize: String, quantity: Int = 1) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _addToCartState.value = AddToCartState.Error("Please login to add items to cart")
            return
        }

        _addToCartState.value = AddToCartState.Loading
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                product = product,
                quantity = quantity,
                selectedSize = selectedSize,
                totalPrice = product.price * quantity
            )
            cartRepository.addToCart(cartItem)
                .onSuccess {
                    _addToCartState.value = AddToCartState.Success
                }
                .onFailure { exception ->
                    _addToCartState.value = AddToCartState.Error(
                        exception.message ?: "Failed to add to cart"
                    )
                }
        }
    }

    fun resetAddToCartState() {
        _addToCartState.value = AddToCartState.Idle
    }
}
