package com.depi.drlist.ui.screens.admin.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: String,
    onNavigateToCustomer: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AdminOrderDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    
    var showStatusDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }
    
    LaunchedEffect(updateState) {
        if (updateState is AdminOrderDetailViewModel.UpdateState.Success) {
            viewModel.resetUpdateState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrder(orderId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is OrderDetailUiState.Success) {
                ExtendedFloatingActionButton(
                    text = { Text("Update Status") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = { showStatusDialog = true }
                )
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is OrderDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is OrderDetailUiState.Success -> {
                OrderDetailContent(
                    order = state.order,
                    onNavigateToCustomer = onNavigateToCustomer,
                    modifier = Modifier.padding(padding)
                )
            }
            is OrderDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadOrder(orderId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
        
        // Loading overlay for update
        if (updateState is AdminOrderDetailViewModel.UpdateState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // Status Update Dialog
    if (showStatusDialog && uiState is OrderDetailUiState.Success) {
        val order = (uiState as OrderDetailUiState.Success).order
        StatusUpdateDialog(
            currentStatus = order.status,
            onConfirm = { newStatus ->
                viewModel.updateOrderStatus(orderId, newStatus)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }
    
    // Error Snackbar
    if (updateState is AdminOrderDetailViewModel.UpdateState.Error) {
        LaunchedEffect(Unit) {
            // Show error message
            viewModel.resetUpdateState()
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: com.depi.drlist.data.model.Order,
    onNavigateToCustomer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Info Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    
                    InfoRow("Order ID", order.orderId.take(12))
                    InfoRow("Date", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(Date(order.orderDate)))
                    InfoRow("Status", order.status)
                    InfoRow("Payment", order.paymentMethod)
                    InfoRow("Total", NumberFormat.getCurrencyInstance(Locale.US).format(order.totalAmount))
                }
            }
        }
        
        // Customer Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToCustomer(order.userId) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Customer Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.ArrowForward, contentDescription = "View customer")
                    }
                    Divider()
                    
                    InfoRow("Customer ID", order.userId.take(12))
                    InfoRow("Shipping Address", order.shippingAddress)
                }
            }
        }
        
        // Order Items
        item {
            Text(
                text = "Order Items",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(order.items) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Size: ${item.selectedSize} | Qty: ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(item.productPrice * item.quantity),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Status History
        if (order.statusHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Status History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(order.statusHistory.reversed()) { change ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = change.status,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    .format(Date(change.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "By: ${change.changedBy.take(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Add spacing at bottom for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusUpdateDialog(
    currentStatus: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    
    val statusOptions = listOf(
        "Pending", "Confirmed", "Shipped", "Delivered", "Cancelled", "Rejected"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Order Status") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Current status: $currentStatus")
                Divider()
                statusOptions.forEach { status ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status }
                        )
                        Text(
                            text = status,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus) },
                enabled = selectedStatus != currentStatus
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
