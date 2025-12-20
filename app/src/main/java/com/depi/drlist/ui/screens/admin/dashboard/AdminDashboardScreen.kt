package com.depi.drlist.ui.screens.admin.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {
                        Text("Quick Actions", fontWeight = FontWeight.Bold)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onNavigateToOrders,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Orders")
                            }

                            OutlinedButton(
                                onClick = onNavigateToProducts,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Products")
                            }

                            OutlinedButton(
                                onClick = onNavigateToAddProduct,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add Product")
                            }
                        }
                    }

                    item {
                        Text("Recent Orders", fontWeight = FontWeight.Bold)
                    }

                    items(state.stats.recentOrders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToOrderDetail(order.orderId) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Order #${order.orderId.take(8)}")
                                Text(
                                    NumberFormat.getCurrencyInstance(Locale.US)
                                        .format(order.totalAmount)
                                )
                                Text(
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        .format(Date(order.orderDate))
                                )
                            }
                        }
                    }
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }
}
