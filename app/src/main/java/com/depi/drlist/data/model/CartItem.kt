package com.depi.drlist.data.model

data class CartItem(
    val productId: String = "",
    val product: Product = Product(),
    val quantity: Int = 1,
    val selectedSize: String = "",
    val totalPrice: Double = 0.0
) {
    fun calculateTotalPrice(): Double {
        return product.price * quantity
    }
}
