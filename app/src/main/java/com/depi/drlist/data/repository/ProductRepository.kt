package com.depi.drlist.data.repository

import com.depi.drlist.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        val listener = firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getProductById(productId: String): Result<Product> {
        return try {
            val doc = firestore.collection("products").document(productId).get().await()
            val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            val snapshot = if (category == "All") {
                firestore.collection("products").get().await()
            } else {
                firestore.collection("products")
                    .whereEqualTo("category", category)
                    .get()
                    .await()
            }
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
