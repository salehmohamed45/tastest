package com.depi.drlist.data.repository

import com.depi.drlist.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val orderId = firestore.collection("orders").document().id
            val orderWithId = order.copy(orderId = orderId)
            firestore.collection("orders").document(orderId).set(orderWithId).await()
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserOrders(): Result<List<Order>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val doc = firestore.collection("orders").document(orderId).get().await()
            val order = doc.toObject(Order::class.java)
            if (order != null) {
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
