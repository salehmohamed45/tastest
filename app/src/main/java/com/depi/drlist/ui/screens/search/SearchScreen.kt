package com.depi.drlist.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.depi.drlist.data.model.Product
import com.depi.drlist.ui.components.LoadingIndicator
import com.depi.drlist.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBackClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: (Product) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Shirts", "Pants", "Jackets", "Shoes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Products", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchProducts(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search for products...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            // Category Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = filterState.category == category,
                        onClick = { viewModel.filterByCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    EmptySearchState()
                }
                is SearchUiState.Loading -> {
                    LoadingIndicator()
                }
                is SearchUiState.Success -> {
                    if (state.products.isEmpty()) {
                        NoResultsState()
                    } else {
                        SearchResults(
                            products = state.products,
                            onProductClick = onProductClick,
                            onAddToCartClick = onAddToCartClick
                        )
                    }
                }
                is SearchUiState.Error -> {
                    ErrorState(message = state.message)
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            filterState = filterState,
            onDismiss = { showFilterDialog = false },
            onApplyFilters = { size, color, brand, priceRange ->
                viewModel.updateFilters(size, color, brand, priceRange)
                showFilterDialog = false
            },
            onClearFilters = {
                viewModel.clearFilters()
                showFilterDialog = false
            }
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = filterState.sortOption,
            onDismiss = { showSortDialog = false },
            onSortSelected = { sortOption ->
                viewModel.updateSortOption(sortOption)
                showSortDialog = false
            }
        )
    }
}

@Composable
fun FilterDialog(
    filterState: FilterState,
    onDismiss: () -> Unit,
    onApplyFilters: (String?, String?, String?, ClosedFloatingPointRange<Float>) -> Unit,
    onClearFilters: () -> Unit
) {
    var selectedSize by remember { mutableStateOf(filterState.selectedSize) }
    var selectedColor by remember { mutableStateOf(filterState.selectedColor) }
    var selectedBrand by remember { mutableStateOf(filterState.selectedBrand) }
    var priceRange by remember { mutableStateOf(filterState.priceRange) }

    val sizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    val colors = listOf("Black", "White", "Blue", "Red", "Gray", "Green")
    val brands = listOf("All", "Nike", "Adidas", "Puma", "Zara", "H&M")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filters") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Size Filter
                Text("Size", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sizes.forEach { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { 
                                selectedSize = if (selectedSize == size) null else size 
                            },
                            label = { Text(size) }
                        )
                    }
                }

                // Color Filter
                Text("Color", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.take(3).forEach { color ->
                        FilterChip(
                            selected = selectedColor == color,
                            onClick = { 
                                selectedColor = if (selectedColor == color) null else color 
                            },
                            label = { Text(color) }
                        )
                    }
                }

                // Brand Filter
                Text("Brand", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    brands.take(3).forEach { brand ->
                        FilterChip(
                            selected = selectedBrand == brand,
                            onClick = { 
                                selectedBrand = if (selectedBrand == brand) null else brand 
                            },
                            label = { Text(brand) }
                        )
                    }
                }

                // Price Range
                Text("Price Range: $${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}", 
                    fontWeight = FontWeight.Bold)
                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = 0f..1000f,
                    steps = 9
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                onApplyFilters(selectedSize, selectedColor, selectedBrand, priceRange) 
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSort: SortOption,
    onDismiss: () -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column {
                SortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == option,
                            onClick = { onSortSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (option) {
                                SortOption.NONE -> "Default"
                                SortOption.PRICE_LOW_TO_HIGH -> "Price: Low to High"
                                SortOption.PRICE_HIGH_TO_LOW -> "Price: High to Low"
                                SortOption.NEWEST -> "Newest First"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SearchResults(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onProductClick = { onProductClick(product) },
                onAddToCartClick = { onAddToCartClick(product) }
            )
        }
    }
}

@Composable
fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Start searching",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Find your perfect style",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoResultsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No products found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try a different search term",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.error
        )
    }
}
