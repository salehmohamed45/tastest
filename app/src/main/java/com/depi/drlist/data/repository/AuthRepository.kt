package com.depi.drlist.data.repository

import com.depi.drlist.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun signUp(name: String, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("User creation failed"))
            
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            // Create user document in Firestore with default role
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = name,
                role = "customer"
            )
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Sign in failed"))
            
            // Fetch user from Firestore
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "",
                role = "customer" // Default to customer for backward compatibility
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth. currentUser ?: return null

        return try {
            // Fetch user from Firestore to get the role
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            userDoc.toObject(User::class.java) ?: User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "",
                role = "customer" // Default
            )
        } catch (e: Exception) {
            // If Firestore fails, return basic user
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?:  "",
                name = firebaseUser.displayName ?: "",
                role = "customer"
            )
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isAdmin(): Boolean {
        val firebaseUser = auth.currentUser ?: return false
        return try {
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
            user?.role == "admin"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isCustomer(): Boolean {
        val firebaseUser = auth.currentUser ?: return false
        return try {
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
            user?.role == "customer"
        } catch (e: Exception) {
            true // Default to customer for backward compatibility
        }
    }
}
