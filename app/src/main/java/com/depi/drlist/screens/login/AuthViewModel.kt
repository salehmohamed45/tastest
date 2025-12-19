package com.depi.drlist.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val message: String) : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- الجزء الجديد ---
    // StateFlow عشان نراقب المستخدم الحالي
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()
    // --- نهاية الجزء الجديد ---

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun signupUser(fullName: String, email: String, password: String) {
        if (fullName.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            _authState.value = AuthState.Error("Please fill in all fields"); return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName.trim()).build()
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                _currentUser.value = auth.currentUser // تحديث المراقب
                                _authState.value = AuthState.Success("Registration successful!")
                            } else {
                                _authState.value = AuthState.Error("Registration successful, but failed to save name.")
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "An error occurred.")
                    }
                }
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            _authState.value = AuthState.Error("Please provide email and password"); return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _currentUser.value = auth.currentUser // تحديث المراقب
                        _authState.value = AuthState.Success("Login successful!")
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Login failed.")
                    }
                }
        }
    }

    // --- دالة جديدة لتسجيل الخروج ---
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
    // --- نهاية الدالة الجديدة ---

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}