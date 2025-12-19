package com.depi.drlist.ui.screens.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.depi.drlist.data.model.Product
import com.depi.drlist.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductDetailViewModel = viewModel(),
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val addToCartState by viewModel.addToCartState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(addToCartState) {
        when (val state = addToCartState) {
            is AddToCartState.Success -> {
                Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartState()
            }
            is AddToCartState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ProductDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is ProductDetailUiState.Success -> {
                ProductDetailContent(
                    product = state.product,
                    modifier = Modifier.padding(padding),
                    onAddToCart = { product, size, quantity ->
                        viewModel.addToCart(product, size, quantity)
                    },
                    isAddingToCart = addToCartState is AddToCartState.Loading
                )
            }
            is ProductDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailContent(
    product: Product,
    modifier: Modifier = Modifier,
    onAddToCart: (Product, String, Int) -> Unit,
    isAddingToCart: Boolean
) {
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Product Image
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Brand and Category
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (product.brand.isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(product.brand) }
                    )
                }
                AssistChip(
                    onClick = { },
                    label = { Text(product.category) }
                )
            }

            // Price
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Divider()

            // Stock Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (product.inStock) Color.Green else Color.Red
                        )
                )
                Text(
                    text = if (product.inStock) "In Stock" else "Out of Stock",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Available Sizes
            if (product.sizes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        product.sizes.forEach { size ->
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                label = { Text(size) }
                            )
                        }
                    }
                }
            }

            // Available Colors
            if (product.colors.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Available Colors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        product.colors.forEach { color ->
                            AssistChip(
                                onClick = { },
                                label = { Text(color) }
                            )
                        }
                    }
                }
            }

            // Quantity Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("-", fontSize = 20.sp)
                    }
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+", fontSize = 20.sp)
                    }
                }
            }

            Divider()

            // Description
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add to Cart Button
            Button(
                onClick = {
                    val size = selectedSize ?: product.sizes.firstOrNull() ?: "M"
                    onAddToCart(product, size, quantity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = product.inStock && !isAddingToCart,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAddingToCart) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Cart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
