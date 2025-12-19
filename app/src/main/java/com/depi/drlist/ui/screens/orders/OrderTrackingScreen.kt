package com.depi.drlist.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBackClick: () -> Unit,
    viewModel: OrderTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAutoRefreshing by viewModel.isAutoRefreshing.collectAsState()
    
    var autoRefreshEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }
    
    LaunchedEffect(autoRefreshEnabled) {
        if (autoRefreshEnabled) {
            viewModel.startAutoRefresh(orderId)
        } else {
            viewModel.stopAutoRefresh()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAutoRefresh()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { autoRefreshEnabled = !autoRefreshEnabled }) {
                        Icon(
                            if (isAutoRefreshing) Icons.Default.CheckCircle else Icons.Default.Refresh,
                            contentDescription = "Auto-refresh",
                            tint = if (isAutoRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.loadOrder(orderId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is OrderTrackingUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is OrderTrackingUiState.Success -> {
                OrderTrackingContent(
                    order = state.order,
                    isAutoRefreshing = isAutoRefreshing,
                    modifier = Modifier.padding(padding)
                )
            }
            is OrderTrackingUiState.Error -> {
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
    }
}

@Composable
private fun OrderTrackingContent(
    order: com.depi.drlist.data.model.Order,
    isAutoRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Auto-refresh indicator
        if (isAutoRefreshing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-refresh enabled (every 30s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Order Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = getStatusColor(order.status)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        getStatusIcon(order.status),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Text(
                        text = order.status,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = getStatusMessage(order.status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        // Order Details Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    
                    InfoRow("Order ID", order.orderId.take(12))
                    InfoRow("Order Date", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(Date(order.orderDate)))
                    InfoRow("Total Amount", NumberFormat.getCurrencyInstance(Locale.US).format(order.totalAmount))
                    InfoRow("Payment Method", order.paymentMethod)
                }
            }
        }
        
        // Tracking Timeline
        item {
            Text(
                text = "Order Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            TrackingTimeline(
                currentStatus = order.status,
                statusHistory = order.statusHistory
            )
        }
        
        // Order Items
        item {
            Text(
                text = "Order Items (${order.items.size})",
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
        
        // Shipping Address
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shipping Address",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = order.shippingAddress,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingTimeline(
    currentStatus: String,
    statusHistory: List<com.depi.drlist.data.model.OrderStatusChange>
) {
    val allStatuses = listOf("Pending", "Confirmed", "Shipped", "Delivered")
    val currentIndex = allStatuses.indexOf(currentStatus).takeIf { it >= 0 } ?: 0
    
    Column {
        allStatuses.forEachIndexed { index, status ->
            val isCompleted = index <= currentIndex
            val isActive = index == currentIndex
            
            TimelineItem(
                status = status,
                isCompleted = isCompleted,
                isActive = isActive,
                isLast = index == allStatuses.lastIndex,
                timestamp = statusHistory.find { it.status == status }?.timestamp
            )
        }
    }
}

@Composable
private fun TimelineItem(
    status: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean,
    timestamp: Long?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isCompleted -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && !isActive) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (timestamp != null) {
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!isLast) {
                Spacer(modifier = Modifier.height(32.dp))
            }
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
private fun getStatusColor(status: String): Color {
    return when (status) {
        "Pending" -> Color(0xFFFFA726)
        "Confirmed" -> Color(0xFF42A5F5)
        "Shipped" -> Color(0xFF66BB6A)
        "Delivered", "Completed" -> Color(0xFF26A69A)
        "Cancelled", "Rejected" -> Color(0xFFEF5350)
        else -> Color.Gray
    }
}

@Composable
private fun getStatusIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        "Pending" -> Icons.Default.DateRange
        "Confirmed" -> Icons.Default.CheckCircle
        "Shipped" -> Icons.Default.LocationOn
        "Delivered", "Completed" -> Icons.Default.Check
        "Cancelled", "Rejected" -> Icons.Default.Close
        else -> Icons.Default.Info
    }
}

private fun getStatusMessage(status: String): String {
    return when (status) {
        "Pending" -> "Your order is being processed"
        "Confirmed" -> "Order confirmed and preparing for shipment"
        "Shipped" -> "Your order is on its way"
        "Delivered", "Completed" -> "Order delivered successfully"
        "Cancelled" -> "Order has been cancelled"
        "Rejected" -> "Order was rejected"
        else -> "Track your order status"
    }
}
