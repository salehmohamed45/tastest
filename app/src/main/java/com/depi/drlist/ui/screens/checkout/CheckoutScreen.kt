package com.depi.drlist.ui.screens.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.depi.drlist.data.model.CartItem
import com.depi.drlist.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = viewModel(),
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var shippingAddress by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.Success) {
            val user = (uiState as CheckoutUiState.Success).user
            if (user?.address != null) {
                shippingAddress = user.address
            }
        } else if (uiState is CheckoutUiState.OrderPlaced) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CheckoutUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is CheckoutUiState.Success -> {
                CheckoutContent(
                    items = state.items,
                    totalAmount = state.totalAmount,
                    shippingAddress = shippingAddress,
                    onAddressChange = { shippingAddress = it },
                    onPlaceOrder = {
                        viewModel.placeOrder(shippingAddress)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is CheckoutUiState.OrderPlaced -> {
                // Show success dialog
            }
            is CheckoutUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = onBackClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    if (showSuccessDialog) {
        OrderSuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                onOrderPlaced()
            }
        )
    }
}

@Composable
fun CheckoutContent(
    items: List<CartItem>,
    totalAmount: Double,
    shippingAddress: String,
    onAddressChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Order Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Order Items
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { cartItem ->
                    OrderItemRow(cartItem = cartItem)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shipping Address
        Text(
            text = "Shipping Address",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = shippingAddress,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your shipping address") },
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.weight(1f))

        // Total and Place Order
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${String.format("%.2f", totalAmount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPlaceOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Place Order",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(cartItem: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cartItem.product.name,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Size: ${cartItem.selectedSize} • Qty: ${cartItem.quantity}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$${String.format("%.2f", cartItem.calculateTotalPrice())}",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "Order Placed Successfully!",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Your order has been placed and will be delivered soon. You can track your order in the Profile section.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue Shopping")
            }
        }
    )
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
                text = "Checkout Error",
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
                Text("Go Back")
            }
        }
    }
}
