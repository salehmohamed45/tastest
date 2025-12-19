package com.depi.drlist.screens.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.depi.drlist.screens.Home.HomeScreen
import com.depi.drlist.screens.login.AuthNavigation
import com.depi.drlist.screens.login.AuthViewModel
import com.depi.drlist.screens.patient.AddPatientScreen
import com.depi.drlist.screens.patient.PatientViewModel
import com.depi.drlist.screens.patient.PatientsListScreen

object Routes {
    const val AUTH = "auth_route"
    const val HOME = "home_route"
    const val ADD_PATIENT = "add_patient_route"
    const val PATIENTS_LIST = "patients_list_route"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val patientViewModel: PatientViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()
    // بنحدد نقطة البداية بناءً على هل المستخدم مسجل دخوله أم لا
    val startDestination = if (currentUser != null) Routes.HOME else Routes.AUTH
    // --- نهاية التعديل ---

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.AUTH) {
            AuthNavigation(
                authViewModel = authViewModel, // مررنا الـ ViewModel هنا
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // --- تعديل: تمرير كل الـ parameters المطلوبة ---
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                patientViewModel = patientViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADD_PATIENT) {
            AddPatientScreen(
                navController = navController,
                patientViewModel = patientViewModel
            )
        }

        composable(Routes.PATIENTS_LIST) {
            PatientsListScreen(
                navController = navController,
                patientViewModel = patientViewModel
            )
        }
    }
}