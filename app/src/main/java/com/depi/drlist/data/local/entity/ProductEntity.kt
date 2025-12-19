package com.depi.drlist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.depi.drlist.data.local.database.Converters

@Entity(tableName = "products")
@TypeConverters(Converters::class)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val sizes: List<String>,
    val colors: List<String>,
    val brand: String,
    val inStock: Boolean,
    val createdAt: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
