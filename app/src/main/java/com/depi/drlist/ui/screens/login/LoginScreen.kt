package com.depi.drlist.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.drlist.R

enum class AuthScreenType { LOGIN, SIGNUP }

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToPasswordReset: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(AuthScreenType.LOGIN) }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Welcome ${state.user.name}!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
                viewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ===== HEADER =====
        AuthHeaderModern()

        Spacer(Modifier.height(24.dp))


        // ===== FORM CARD =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-24).dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ===== TAB SWITCHER =====
                AuthTabSwitcher(
                    selected = currentScreen,
                    onSelect = { currentScreen = it }
                )

                Spacer(Modifier.height(24.dp))

                when (currentScreen) {
                    AuthScreenType.LOGIN ->
                        LoginForm(
                            viewModel = viewModel,
                            authState = authState,
                            onNavigateToPasswordReset = onNavigateToPasswordReset
                        )

                    AuthScreenType.SIGNUP ->
                        SignUpForm(
                            viewModel = viewModel,
                            authState = authState
                        )
                }
            }
        }
    }
}

@Composable
private fun AuthHeaderModern() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E3C72),
                        Color(0xFF2A5298)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(160.dp)
                    .padding(top = 24.dp)
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "TAS Collection",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Modern style. Premium quality.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AuthTabSwitcher(
    selected: AuthScreenType,
    onSelect: (AuthScreenType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(50)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AuthTab(
            text = "Login",
            selected = selected == AuthScreenType.LOGIN,
            onClick = { onSelect(AuthScreenType.LOGIN) }
        )
        AuthTab(
            text = "Sign Up",
            selected = selected == AuthScreenType.SIGNUP,
            onClick = { onSelect(AuthScreenType.SIGNUP) }
        )
    }
}

@Composable
private fun AuthTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent,
            contentColor =
                if (selected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 6.dp else 0.dp
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginForm(
    viewModel: LoginViewModel,
    authState: AuthState,
    onNavigateToPasswordReset: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToPasswordReset,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.signIn(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = authState != AuthState.Loading
        ) {
            if (authState == AuthState.Loading)
                CircularProgressIndicator(color = Color.White)
            else
                Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SignUpForm(
    viewModel: LoginViewModel,
    authState: AuthState
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signUp(fullName, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = authState != AuthState.Loading
        ) {
            if (authState == AuthState.Loading)
                CircularProgressIndicator(color = Color.White)
            else
                Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
