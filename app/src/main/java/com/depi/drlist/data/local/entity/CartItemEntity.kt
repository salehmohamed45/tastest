package com.depi.drlist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val productId: String,
    val productName: String,
    val productImageUrl: String,
    val productPrice: Double,
    val selectedSize: String,
    val quantity: Int
)
