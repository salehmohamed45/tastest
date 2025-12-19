package com.depi.drlist.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val brand: String = "",
    val inStock: Boolean = true,
    val createdAt: Long = 0L
)
