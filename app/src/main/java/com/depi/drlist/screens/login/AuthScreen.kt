package com.depi.drlist.screens.login
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

enum class AuthScreenType {
    LOGIN,
    SIGNUP
}

@Composable
fun AuthHeader() {
    Column(
        modifier = Modifier.fillMaxWidth().background(
            brush = Brush.verticalGradient(colors = listOf(Color(0xFF4A90E2), Color(0xFF6AABF2))),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ).padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Outlined.MedicalServices, contentDescription = "Clinic Logo", tint = Color.White, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Clinic Manager", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
        Text(text = "Professional Patient Management System", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
fun AuthTabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF4A90E2) else Color.Transparent, contentColor = if (isSelected) Color.White else Color.Gray),
        shape = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Text(text)
    }
}

@Composable
fun AuthNavigation(onLoginSuccess: () -> Unit, authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf(AuthScreenType.LOGIN) }
    val authViewModel: AuthViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        AuthHeader()
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthTabButton(text = "Login", isSelected = currentScreen == AuthScreenType.LOGIN, onClick = { currentScreen = AuthScreenType.LOGIN })
                AuthTabButton(text = "Register", isSelected = currentScreen == AuthScreenType.SIGNUP, onClick = { currentScreen = AuthScreenType.SIGNUP })
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (currentScreen == AuthScreenType.LOGIN) {
                LoginScreen(authViewModel, onLoginSuccess = onLoginSuccess)
            } else {
                SignupScreen(authViewModel, onSignupSuccess = onLoginSuccess)
            }
        }
    }
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                onLoginSuccess() // Notify the navigation that login was successful
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { authViewModel.loginUser(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                enabled = authState != AuthState.Loading
            ) {
                Text("Login", fontSize = 18.sp)
            }
        }
        if (authState == AuthState.Loading) {
            CircularProgressIndicator()
        }
    }
}
@Composable
fun SignupScreen(authViewModel: AuthViewModel, onSignupSuccess: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                onSignupSuccess()
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    // *** THIS IS THE CHANGE ***
                    authViewModel.signupUser(fullName, email, password)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50B8B4)),
                enabled = authState != AuthState.Loading
            ) {
                Text("Register", fontSize = 18.sp)
            }
        }
        if (authState == AuthState.Loading) {
            CircularProgressIndicator()
        }
    }
}