package com.depi.drlist.data.repository

import com.depi.drlist.data.model.Order
import com.depi.drlist.data.model.OrderStatusChange
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

    // Admin methods
    suspend fun getAllOrders(): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
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

    suspend fun searchOrders(query: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }.filter { order ->
                order.orderId.contains(query, ignoreCase = true) ||
                order.userId.contains(query, ignoreCase = true) ||
                order.status.contains(query, ignoreCase = true)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val orderDoc = firestore.collection("orders").document(orderId)
            val order = orderDoc.get().await().toObject(Order::class.java)
                ?: return Result.failure(Exception("Order not found"))
            
            val statusChange = OrderStatusChange(
                status = newStatus,
                timestamp = System.currentTimeMillis(),
                changedBy = userId
            )
            
            val updatedHistory = order.statusHistory + statusChange
            orderDoc.update(
                mapOf(
                    "status" to newStatus,
                    "statusHistory" to updatedHistory
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersByStatus(status: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("status", status)
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
}
