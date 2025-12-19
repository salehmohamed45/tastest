package com.depi.drlist.ui.screens.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Product
import com.depi.drlist.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddProductUiState {
    object Idle : AddProductUiState()
    object Loading : AddProductUiState()
    object Success : AddProductUiState()
    data class Error(val message: String) : AddProductUiState()
}

class AddProductViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    
    private val _uiState = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()
    
    fun addProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = AddProductUiState.Loading
            
            productRepository.addProduct(product).fold(
                onSuccess = {
                    _uiState.value = AddProductUiState.Success
                },
                onFailure = { exception ->
                    _uiState.value = AddProductUiState.Error(
                        exception.message ?: "Failed to add product"
                    )
                }
            )
        }
    }
    
    fun resetState() {
        _uiState.value = AddProductUiState.Idle
    }
    
    fun validateImageUrl(url: String): Boolean {
        if (url.isBlank()) return false
        
        val validExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
        val lowerUrl = url.lowercase()
        
        return (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://")) &&
                validExtensions.any { lowerUrl.contains(it) }
    }
}
