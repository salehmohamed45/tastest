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

enum class SortOption {
    NONE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    NEWEST
}

data class FilterState(
    val category: String = "All",
    val selectedSize: String? = null,
    val selectedColor: String? = null,
    val selectedBrand: String? = null,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    val sortOption: SortOption = SortOption.NONE
)

class SearchViewModel : ViewModel() {
    private val productRepository = ProductRepository()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            productRepository.searchProducts(query)
                .onSuccess { products ->
                    allProducts = products
                    applyFiltersAndSort()
                }
                .onFailure { exception ->
                    _uiState.value = SearchUiState.Error(exception.message ?: "Search failed")
                }
        }
    }

    fun filterByCategory(category: String) {
        _filterState.value = _filterState.value.copy(category = category)
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            productRepository.getProductsByCategory(category)
                .onSuccess { products ->
                    allProducts = products
                    applyFiltersAndSort()
                }
                .onFailure { exception ->
                    _uiState.value = SearchUiState.Error(exception.message ?: "Filter failed")
                }
        }
    }

    fun updateFilters(
        size: String? = null,
        color: String? = null,
        brand: String? = null,
        priceRange: ClosedFloatingPointRange<Float>? = null
    ) {
        _filterState.value = _filterState.value.copy(
            selectedSize = size,
            selectedColor = color,
            selectedBrand = brand,
            priceRange = priceRange ?: _filterState.value.priceRange
        )
        applyFiltersAndSort()
    }

    fun updateSortOption(sortOption: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sortOption)
        applyFiltersAndSort()
    }

    fun clearFilters() {
        _filterState.value = FilterState(category = _filterState.value.category)
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        var filteredProducts = allProducts

        // Apply filters
        val state = _filterState.value
        
        if (state.selectedSize != null) {
            filteredProducts = filteredProducts.filter { 
                it.sizes.contains(state.selectedSize) 
            }
        }
        
        if (state.selectedColor != null) {
            filteredProducts = filteredProducts.filter { 
                it.colors.contains(state.selectedColor) 
            }
        }
        
        if (state.selectedBrand != null && state.selectedBrand != "All") {
            filteredProducts = filteredProducts.filter { 
                it.brand == state.selectedBrand 
            }
        }
        
        filteredProducts = filteredProducts.filter {
            it.price in state.priceRange.start.toDouble()..state.priceRange.endInclusive.toDouble()
        }

        // Apply sorting
        filteredProducts = when (state.sortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> filteredProducts.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> filteredProducts.sortedByDescending { it.price }
            SortOption.NEWEST -> filteredProducts.sortedByDescending { it.createdAt }
            SortOption.NONE -> filteredProducts
        }

        _uiState.value = SearchUiState.Success(filteredProducts)
    }
}
