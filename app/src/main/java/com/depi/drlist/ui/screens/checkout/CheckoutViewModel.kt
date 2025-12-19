package com.depi.drlist.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.CartItem
import com.depi.drlist.data.model.Order
import com.depi.drlist.data.model.User
import com.depi.drlist.data.repository.AuthRepository
import com.depi.drlist.data.repository.CartRepository
import com.depi.drlist.data.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckoutUiState {
    data object Loading : CheckoutUiState()
    data class Success(val items: List<CartItem>, val user: User?, val totalAmount: Double) : CheckoutUiState()
    data object ProcessingPayment : CheckoutUiState()
    data class OrderPlaced(val orderId: String) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    CASH_ON_DELIVERY,
    PAYPAL
}

class CheckoutViewModel : ViewModel() {
    private val cartRepository = CartRepository()
    private val orderRepository = OrderRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var cartItems: List<CartItem> = emptyList()

    init {
        loadCheckoutData()
    }

    private fun loadCheckoutData() {
        viewModelScope.launch {
            try {
                cartRepository.getCartItems().collect { items ->
                    cartItems = items
                    val totalAmount = items.sumOf { it.calculateTotalPrice() }
                    val user = authRepository.getCurrentUser()
                    _uiState.value = CheckoutUiState.Success(items, user, totalAmount)
                }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error(e.message ?: "Failed to load checkout data")
            }
        }
    }

    fun placeOrder(shippingAddress: String, paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _uiState.value = CheckoutUiState.Error("User not logged in")
                    return@launch
                }

                if (shippingAddress.isBlank()) {
                    _uiState.value = CheckoutUiState.Error("Please enter a shipping address")
                    return@launch
                }

                // Process payment (mock implementation)
                _uiState.value = CheckoutUiState.ProcessingPayment
                val paymentSuccess = processPayment(paymentMethod)
                
                if (!paymentSuccess) {
                    _uiState.value = CheckoutUiState.Error("Payment failed. Please try again.")
                    return@launch
                }

                val totalAmount = cartItems.sumOf { it.calculateTotalPrice() }
                val order = Order(
                    userId = user.uid,
                    items = cartItems,
                    totalAmount = totalAmount,
                    orderDate = System.currentTimeMillis(),
                    status = "Pending",
                    shippingAddress = shippingAddress,
                    paymentMethod = paymentMethod.name
                )

                orderRepository.placeOrder(order)
                    .onSuccess { orderId ->
                        cartRepository.clearCart()
                        _uiState.value = CheckoutUiState.OrderPlaced(orderId)
                    }
                    .onFailure { exception ->
                        _uiState.value = CheckoutUiState.Error(exception.message ?: "Failed to place order")
                    }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error(e.message ?: "Failed to place order")
            }
        }
    }

    private suspend fun processPayment(paymentMethod: PaymentMethod): Boolean {
        // Mock payment processing with delay to simulate API call
        delay(2000)
        
        // For now, all payments succeed except we can add validation later
        return when (paymentMethod) {
            PaymentMethod.CASH_ON_DELIVERY -> true // Always succeeds
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.DEBIT_CARD,
            PaymentMethod.PAYPAL -> true // Mock success, would validate with actual gateway
        }
    }
}
