package com.depi.drlist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.depi.drlist.data.local.database.Converters

@Entity(tableName = "orders")
@TypeConverters(Converters::class)
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val items: List<OrderItemData>,
    val totalAmount: Double,
    val shippingAddress: String,
    val status: String,
    val createdAt: Long
)

data class OrderItemData(
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val selectedSize: String
)
