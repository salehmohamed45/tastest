package com.depi.drlist.screens.patient

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(navController: NavController, patientViewModel: PatientViewModel = viewModel()) {
    val patientName by patientViewModel.patientName.collectAsState()
    val nationalId by patientViewModel.nationalId.collectAsState()
    val age by patientViewModel.age.collectAsState()
    val address by patientViewModel.address.collectAsState()
    val visitType by patientViewModel.visitType.collectAsState()
    val visitPrice by patientViewModel.visitPrice.collectAsState()

    val saveState by patientViewModel.saveState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                patientViewModel.resetSaveState()
                navController.popBackStack()
            }
            is SaveState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                patientViewModel.resetSaveState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            // هنستخدم الهيدر المخصص بتاعنا هنا
            AddPatientHeader(navController = navController)
        },
        bottomBar = {
            // --- هنا مكان الزرار الجديد ---
            // هنحط الزرار في BottomAppBar عشان يثبت تحت
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { patientViewModel.addPatient() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50B8B4)),
                    enabled = saveState != SaveState.Loading
                ) {
                    if (saveState == SaveState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = "Save Patient")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Patient", fontSize = 18.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        // دي المساحة اللي جواها الفورم وهتكون قابلة للـ scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues) // مهم نستخدم الـ padding ده
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Patient Name", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            OutlinedTextField(value = patientName, onValueChange = patientViewModel::onPatientNameChange, placeholder = { Text("Enter patient name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Text("National ID", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            OutlinedTextField(value = nationalId, onValueChange = patientViewModel::onNationalIdChange, placeholder = { Text("Enter national ID") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true)

            Text("Age", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            OutlinedTextField(value = age, onValueChange = patientViewModel::onAgeChange, placeholder = { Text("Enter age") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true)

            Text("Address", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            OutlinedTextField(value = address, onValueChange = patientViewModel::onAddressChange, placeholder = { Text("Enter address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Text("Visit Type", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            VisitTypeDropdown(selectedType = visitType, onTypeSelected = patientViewModel::onVisitTypeChange)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachMoney, contentDescription = "Money", tint = Color(0xFF00796B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit Price", modifier = Modifier.weight(1f), color = Color(0xFF00796B))
                    Text("$visitPrice EGP", color = Color(0xFF00796B), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitTypeDropdown(selectedType: String, onTypeSelected: (String) -> Unit) {
    val visitTypes = listOf("كشف", "إعادة")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select visit type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4A90E2),
                unfocusedBorderColor = Color.LightGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            visitTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Custom header for AddPatientScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF4A90E2), // نفس لون Header الرئيسي
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .height(160.dp) // ارتفاع ثابت للهيدر
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Add New Patient",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fill in patient information",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AddPatientScreenPreview() {
    AddPatientScreen(navController = rememberNavController())
}