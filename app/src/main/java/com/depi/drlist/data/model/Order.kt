package com.depi.drlist.data.model

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "Pending",
    val shippingAddress: String = "",
    val paymentMethod: String = "CASH_ON_DELIVERY"
)
