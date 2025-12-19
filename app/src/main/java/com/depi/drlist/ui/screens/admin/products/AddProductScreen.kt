package com.depi.drlist.ui.screens.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.depi.drlist.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onProductAdded: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddProductViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Shirts") }
    var brand by remember { mutableStateOf("") }
    var sizes by remember { mutableStateOf("S,M,L,XL") }
    var colors by remember { mutableStateOf("Black,White,Blue") }
    var inStock by remember { mutableStateOf(true) }
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    var imageUrlError by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState) {
        if (uiState is AddProductUiState.Success) {
            onProductAdded()
            viewModel.resetState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
            
            // Price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (USD) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
            )
            
            // Image URL
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { 
                    imageUrl = it
                    imageUrlError = false
                },
                label = { Text("Image URL *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = imageUrlError,
                supportingText = {
                    if (imageUrlError) {
                        Text("Please enter a valid image URL (http/https with .jpg, .png, etc.)")
                    } else {
                        Text("Enter a direct link to the product image")
                    }
                },
                leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null) },
                trailingIcon = {
                    if (imageUrl.isNotEmpty() && !imageUrlError) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
            
            // Category
            OutlinedTextField(
                value = category,
                onValueChange = { },
                label = { Text("Category *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                    }
                },
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
            )
            
            // Brand
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
            )
            
            // Sizes
            OutlinedTextField(
                value = sizes,
                onValueChange = { sizes = it },
                label = { Text("Available Sizes (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Example: S,M,L,XL") },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
            
            // Colors
            OutlinedTextField(
                value = colors,
                onValueChange = { colors = it },
                label = { Text("Available Colors (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Example: Black,White,Blue") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
            
            // In Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product In Stock",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = inStock,
                    onCheckedChange = { inStock = it }
                )
            }
            
            Divider()
            
            // Error Message
            if (uiState is AddProductUiState.Error) {
                Text(
                    text = (uiState as AddProductUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Add Button
            Button(
                onClick = {
                    // Validate required fields
                    if (name.isBlank() || description.isBlank() || price.isBlank() || imageUrl.isBlank()) {
                        return@Button
                    }
                    
                    // Validate image URL
                    if (!viewModel.validateImageUrl(imageUrl)) {
                        imageUrlError = true
                        return@Button
                    }
                    
                    val priceValue = price.toDoubleOrNull()
                    if (priceValue == null || priceValue <= 0) {
                        return@Button
                    }
                    
                    val product = Product(
                        name = name,
                        description = description,
                        price = priceValue,
                        imageUrl = imageUrl,
                        category = category,
                        brand = brand,
                        sizes = sizes.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        colors = colors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        inStock = inStock
                    )
                    
                    viewModel.addProduct(product)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddProductUiState.Loading
            ) {
                if (uiState is AddProductUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Product")
                }
            }
        }
    }
    
    // Category Selection Dialog
    if (showCategoryDialog) {
        val categories = listOf("Shirts", "Pants", "Jackets", "Shoes", "Accessories")
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = category == cat,
                                onClick = {
                                    category = cat
                                    showCategoryDialog = false
                                }
                            )
                            Text(
                                text = cat,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
