package com.depi.drlist.screens.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.depi.drlist.screens.login.AuthViewModel
import com.depi.drlist.screens.navigation.Routes
import com.depi.drlist.screens.patient.PatientViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    patientViewModel: PatientViewModel,
    onSignOut: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val doctorName = currentUser?.displayName ?: "Doctor"
    val patientCount by patientViewModel.todaysPatientCount.collectAsState()
    val income by patientViewModel.todaysIncome.collectAsState()
    val weeklyPatientCount by patientViewModel.weeklyPatientCount.collectAsState()
    val weeklyIncome by patientViewModel.weeklyIncome.collectAsState()
    val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

    // This is the key to the solution: It tells the ViewModel to fetch data
    // only when the user is confirmed to be logged in.
    LaunchedEffect(key1 = currentUser) {
        if (currentUser != null) {
            patientViewModel.fetchPatients()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(
                Color(0xFF4A90E2),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ).padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Welcome back,", color = Color.White.copy(alpha = 0.8f))
                    Text("Dr $doctorName", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Outlined.Logout, contentDescription = "Sign Out", tint = Color.White)
                }
            }
            Text(currentDate, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Today's Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard("Patients", patientCount.toString(), Icons.Outlined.People, Modifier.weight(1f))
                SummaryCard("Income", income.toInt().toString(), Icons.Outlined.AttachMoney, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Quick Actions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            QuickActionButton("Add New Patient", Icons.Outlined.Add, isPrimary = true, onClick = { navController.navigate(Routes.ADD_PATIENT) })
            Spacer(modifier = Modifier.height(12.dp))
            QuickActionButton("View Patients", Icons.Outlined.ListAlt, onClick = { navController.navigate(Routes.PATIENTS_LIST) })

            Spacer(modifier = Modifier.height(24.dp))
            OverallStatsCard(
                weeklyPatientCount = weeklyPatientCount,
                weeklyIncome = weeklyIncome.toInt()
            )
        }
    }
}

@Composable
fun OverallStatsCard(weeklyPatientCount: Int, weeklyIncome: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = "Overall Statistics", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Overall Statistics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total Patients", color = Color.Gray, fontSize = 16.sp)
                Text("$weeklyPatientCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4A90E2))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total Income", color = Color.Gray, fontSize = 16.sp)
                Text("$weeklyIncome EGP", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF50B8B4))
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.Gray)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, isPrimary: Boolean = false, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isPrimary) Color(0xFF4A90E2) else Color(0xFF50B8B4), contentColor = Color.White)) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}