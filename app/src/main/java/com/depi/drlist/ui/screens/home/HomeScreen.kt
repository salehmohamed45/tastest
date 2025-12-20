package com.depi.drlist.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.depi.drlist.data.model.Product
import com.depi.drlist.ui.components.LoadingIndicator
import com.depi.drlist.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onCartClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAdminDashboardClick: () -> Unit = {},
    isAdmin: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSizeDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TAS Collection",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.statusBarsPadding(),

                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onAdminDashboardClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Admin Dashboard"
                            )
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (uiState is HomeUiState.Success) {
                                val count = (uiState as HomeUiState.Success).cartItemCount
                                if (count > 0) {
                                    Badge { Text(count.toString()) }
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart"
                            )
                        }
                    }
                }
            )
        },modifier = Modifier.statusBarsPadding()
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is HomeUiState.Success -> {
                if (state.products.isEmpty()) {
                    EmptyProductsState(modifier = Modifier.padding(paddingValues))
                } else {
                    ProductGrid(
                        products = state.products,
                        onProductClick = onProductClick,
                        onAddToCartClick = { product ->
                            if (product.sizes.isNotEmpty()) {
                                selectedProduct = product
                                showSizeDialog = true
                            } else {
                                viewModel.addToCart(product, "One Size")
                                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            is HomeUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Size Selection Dialog
    if (showSizeDialog && selectedProduct != null) {
        SizeSelectionDialog(
            product = selectedProduct!!,
            onSizeSelected = { size ->
                viewModel.addToCart(selectedProduct!!, size)
                Toast.makeText(
                    context,
                    "${selectedProduct!!.name} (Size $size) added to cart",
                    Toast.LENGTH_SHORT
                ).show()
                showSizeDialog = false
                selectedProduct = null
            },
            onDismiss = {
                showSizeDialog = false
                selectedProduct = null
            }
        )
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
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
fun SizeSelectionDialog(
    product: Product,
    onSizeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Size") },
        text = {
            Column {
                Text("Choose a size for ${product.name}")
                Spacer(modifier = Modifier.height(16.dp))
                product.sizes.forEach { size ->
                    Button(
                        onClick = { onSizeSelected(size) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(size)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyProductsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No products available",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Check back later for new arrivals",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Oops! Something went wrong",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
