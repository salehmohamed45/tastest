package com.depi.drlist.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.depi.drlist.data.model.Patient
import com.depi.drlist.screens.Home.SummaryCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsListScreen(navController: NavController, patientViewModel: PatientViewModel = viewModel()) {
    val filteredPatientsState by patientViewModel.filteredPatientsState.collectAsState(initial = PatientListState.Loading)
    val totalPatientCount by patientViewModel.totalPatientCount.collectAsState()
    val totalIncome by patientViewModel.totalIncome.collectAsState()
    val searchQuery by patientViewModel.searchQuery.collectAsState()
    val fromDate by patientViewModel.fromDate.collectAsState()
    val toDate by patientViewModel.toDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf("from") }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    if (datePickerTarget == "from") {
                        patientViewModel.onFromDateSelected(datePickerState.selectedDateMillis)
                    } else {
                        patientViewModel.onToDateSelected(datePickerState.selectedDateMillis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { PatientsListHeader(navController) },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard("Total Patients", totalPatientCount.toString(), Icons.Outlined.People, Modifier.weight(1f))
                SummaryCard("Total Income", "${totalIncome.toInt()} EGP", Icons.Outlined.AttachMoney, Modifier.weight(1f))
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = patientViewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or ID...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            DateFilterCard(
                fromDate = fromDate,
                toDate = toDate,
                onFromClick = { datePickerTarget = "from"; showDatePicker = true },
                onToClick = { datePickerTarget = "to"; showDatePicker = true }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = filteredPatientsState) {
                    is PatientListState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is PatientListState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                    is PatientListState.Success -> {
                        if (state.patients.isEmpty()) {
                            Text("No patients found.", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.patients) { patient -> PatientItem(patient = patient) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsListHeader(navController: NavController) {
    TopAppBar(
        title = {
            Column {
                Text("Patients List", fontWeight = FontWeight.Bold)
                Text("View and manage all patients", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        },

        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF4A90E2),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun DateFilterCard(fromDate: Date?, toDate: Date?, onFromClick: () -> Unit, onToClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = "Date Filter")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Date Filter", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = fromDate?.toFormattedString() ?: "",
                    onValueChange = {},
                    label = { Text("From") },
                    modifier = Modifier.weight(1f).clickable(onClick = onFromClick),
                    readOnly = true, enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = LocalContentColor.current)
                )
                OutlinedTextField(
                    value = toDate?.toFormattedString() ?: "",
                    onValueChange = {},
                    label = { Text("To") },
                    modifier = Modifier.weight(1f).clickable(onClick = onToClick),
                    readOnly = true, enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = LocalContentColor.current)
                )
            }
        }
    }
}

@Composable
fun PatientItem(patient: Patient) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(patient.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${patient.visitPrice} EGP", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF00796B))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("ID: ${patient.nationalId}", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = patient.visitType, modifier = Modifier.background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF00796B), fontSize = 12.sp)
                Text(text = patient.visitDate?.toFormattedString() ?: "", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

fun Date.toFormattedString(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(this)
}