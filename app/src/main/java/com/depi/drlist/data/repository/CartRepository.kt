package com.depi.drlist.data.repository

import com.depi.drlist.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun getUserCartRef() = auth.currentUser?.uid?.let { userId ->
        firestore.collection("carts").document(userId).collection("items")
    }

    fun getCartItems(): Flow<List<CartItem>> = callbackFlow {
        val cartRef = getUserCartRef()
        if (cartRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = cartRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(CartItem::class.java)
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addToCart(cartItem: CartItem): Result<Unit> {
        return try {
            val cartRef = getUserCartRef() ?: return Result.failure(Exception("User not logged in"))
            val docRef = cartRef.document(cartItem.productId)
            
            // Check if item already exists
            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                val existingItem = existingDoc.toObject(CartItem::class.java)
                val newQuantity = (existingItem?.quantity ?: 0) + cartItem.quantity
                docRef.update("quantity", newQuantity).await()
            } else {
                docRef.set(cartItem).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCartItemQuantity(productId: String, quantity: Int): Result<Unit> {
        return try {
            val cartRef = getUserCartRef() ?: return Result.failure(Exception("User not logged in"))
            if (quantity <= 0) {
                cartRef.document(productId).delete().await()
            } else {
                cartRef.document(productId).update("quantity", quantity).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(productId: String): Result<Unit> {
        return try {
            val cartRef = getUserCartRef() ?: return Result.failure(Exception("User not logged in"))
            cartRef.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(): Result<Unit> {
        return try {
            val cartRef = getUserCartRef() ?: return Result.failure(Exception("User not logged in"))
            val snapshot = cartRef.get().await()
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
