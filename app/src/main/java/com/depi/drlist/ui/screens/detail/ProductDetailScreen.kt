package com.depi.drlist.ui.screens.detail

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                Toast.makeText(context, "Product added to cart successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartState()
            }
            is AddToCartState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddToCartState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Product Details",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "View shopping cart"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    onAddToCart = { product: Product, size: String, quantity: Int ->
                        viewModel.addToCart(product, size, quantity)
                    },
                    isAddingToCart = addToCartState is AddToCartState.Loading
                )
            }
            is ProductDetailUiState.Error -> {
                ErrorState(
                    message = state.message,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unable to Load Product",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
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
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Product Image with elevated card style
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = "Image of ${product.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Product Header Section
            ProductHeaderSection(product = product)

            HorizontalDivider(thickness = 1.dp)

            // Stock and Shipping Info
            InfoCardsSection(product = product)

            // Size Selection
            if (product.sizes.isNotEmpty()) {
                SizeSelector(
                    sizes = product.sizes,
                    selectedSize = selectedSize,
                    onSizeSelected = { selectedSize = it }
                )
            }

            // Color Selection
            if (product.colors.isNotEmpty()) {
                ColorSelector(
                    colors = product.colors,
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }

            // Quantity Selector
            QuantitySelector(
                quantity = quantity,
                onQuantityChange = { quantity = it }
            )

            HorizontalDivider(thickness = 1.dp)

            // Description Section
            ProductDescription(description = product.description)

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Cart Button
            AddToCartButton(
                enabled = product.inStock && !isAddingToCart,
                isLoading = isAddingToCart,
                onClick = {
                    val size = selectedSize ?: product.sizes.firstOrNull() ?: "M"
                    onAddToCart(product, size, quantity)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProductHeaderSection(product: Product) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Product Name
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        )

        // Brand and Category
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.brand.isNotEmpty()) {
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            product.brand,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            SuggestionChip(
                onClick = { },
                label = { Text(product.category) }
            )
        }

        // Price
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$${String.format("%.2f", product.price)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            // Rating stars placeholder
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Rating",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFFA000)
                )
                Text(
                    text = "4.5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoCardsSection(product: Product) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(
            icon = if (product.inStock) Icons.Outlined.CheckCircle else Icons.Outlined.CheckCircle,
            title = if (product.inStock) "In Stock" else "Out of Stock",
            subtitle = if (product.inStock) "Ready to ship" else "Unavailable",
            containerColor = if (product.inStock)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.weight(1f)
        )

        InfoCard(
            icon = Icons.Outlined.LocalShipping,
            title = "Free Shipping",
            subtitle = "On orders $50+",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SizeSelector(
    sizes: List<String>,
    selectedSize: String?,
    onSizeSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Select Size",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sizes.forEach { size ->
                FilterChip(
                    selected = selectedSize == size,
                    onClick = { onSizeSelected(size) },
                    label = {
                        Text(
                            size,
                            fontWeight = if (selectedSize == size) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = if (selectedSize == size) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSelector(
    colors: List<String>,
    selectedColor: String?,
    onColorSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Available Colors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.forEach { color ->
                ColorButton(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorButton(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = getColorFromName(color)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            border = BorderStroke(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            ),
            shadowElevation = if (isSelected) 8.dp else 2.dp
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Selected",
                        tint = if (isColorDark(backgroundColor)) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Text(
            text = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getColorFromName(colorName: String): Color {
    return when (colorName.lowercase()) {
        "black" -> Color(0xFF000000)
        "white" -> Color(0xFFFFFFFF)
        "red" -> Color(0xFFE53935)
        "blue" -> Color(0xFF1E88E5)
        "green" -> Color(0xFF43A047)
        "yellow" -> Color(0xFFFDD835)
        "orange" -> Color(0xFFFF6F00)
        "purple" -> Color(0xFF8E24AA)
        "pink" -> Color(0xFFD81B60)
        "brown" -> Color(0xFF6D4C41)
        "gray", "grey" -> Color(0xFF757575)
        "navy" -> Color(0xFF1A237E)
        "teal" -> Color(0xFF00897B)
        "lime" -> Color(0xFFCDDC39)
        "indigo" -> Color(0xFF3949AB)
        "cyan" -> Color(0xFF00ACC1)
        "beige" -> Color(0xFFF5F5DC)
        "maroon" -> Color(0xFF800000)
        "olive" -> Color(0xFF808000)
        "gold" -> Color(0xFFFFD700)
        "silver" -> Color(0xFFC0C0C0)
        else -> Color(0xFF9E9E9E) // Default gray for unknown colors
    }
}

private fun isColorDark(color: Color): Boolean {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return luminance < 0.5
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                modifier = Modifier.size(48.dp),
                enabled = quantity > 1,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "−",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            FilledIconButton(
                onClick = { onQuantityChange(quantity + 1) },
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProductDescription(description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Product Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun AddToCartButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "Add to cart button animation"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add to Cart",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}