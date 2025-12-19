package com.depi.drlist.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.drlist.data.model.Product
import com.depi.drlist.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data class Success(val products: List<Product>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel : ViewModel() {
    private val productRepository = ProductRepository()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            productRepository.searchProducts(query)
                .onSuccess { products ->
                    _uiState.value = SearchUiState.Success(products)
                }
                .onFailure { exception ->
                    _uiState.value = SearchUiState.Error(exception.message ?: "Search failed")
                }
        }
    }

    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            productRepository.getProductsByCategory(category)
                .onSuccess { products ->
                    _uiState.value = SearchUiState.Success(products)
                }
                .onFailure { exception ->
                    _uiState.value = SearchUiState.Error(exception.message ?: "Filter failed")
                }
        }
    }
}
