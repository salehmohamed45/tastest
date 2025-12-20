package com.depi.drlist.ui.screens.admin.dashboard

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {

            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Success -> {
                DashboardContent(
                    stats = state.stats,
                    onNavigateToOrders = onNavigateToOrders,
                    onNavigateToProducts = onNavigateToProducts, // ✅
                    onNavigateToAddProduct = onNavigateToAddProduct,
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    modifier = Modifier.padding(padding)
                )
            }

            is DashboardUiState.Error -> {
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
                        Button(onClick = { viewModel.loadDashboardData() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    onNavigateToOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,   // ✅ ADDED
    onNavigateToAddProduct: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ===== OVERVIEW =====
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Orders",
                    value = stats.totalOrders.toString(),
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending",
                    value = stats.pendingOrders.toString(),
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Completed",
                    value = stats.completedOrders.toString(),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Revenue",
                    value = NumberFormat
                        .getCurrencyInstance(Locale.US)
                        .format(stats.totalRevenue),
                    icon = Icons.Default.AccountCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ===== QUICK ACTIONS =====
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = onNavigateToOrders,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.List, contentDescription = null)
                        Spacer(Modifier.height(4.dp))
                        Text("View Orders", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToProducts,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                        Spacer(Modifier.height(4.dp))
                        Text("Products", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToAddProduct,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.height(4.dp))
                        Text("Add Product", fontSize = 12.sp)
                    }
                }
            }
        }

        // ===== RECENT ORDERS =====
        item {
            Text(
                text = "Recent Orders",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (stats.recentOrders.isEmpty()) {
            item {
                Text(
                    text = "No recent orders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(stats.recentOrders) { order ->
                OrderSummaryCard(
                    order = order,
                    onClick = { onNavigateToOrderDetail(order.orderId) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderSummaryCard(
    order: com.depi.drlist.data.model.Order,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                    text = "Order #${order.orderId.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = order.status)
            }

            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(Date(order.orderDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(order.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val containerColor = when (status) {
        "Pending" -> MaterialTheme.colorScheme.tertiary
        "Confirmed" -> MaterialTheme.colorScheme.primary
        "Shipped" -> MaterialTheme.colorScheme.secondary
        "Delivered", "Completed" -> MaterialTheme.colorScheme.primaryContainer
        "Cancelled", "Rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
