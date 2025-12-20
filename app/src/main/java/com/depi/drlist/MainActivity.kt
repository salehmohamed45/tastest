package com.depi.drlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.depi.drlist.ui.navigation.AppNavigation
import com.depi.drlist.ui.theme.DrListTheme
import com.depi.drlist.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize theme ViewModel
        themeViewModel = ThemeViewModel(applicationContext)

        enableEdgeToEdge()

        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            DrListTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize().statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(themeViewModel = themeViewModel)
                }
            }
        }
    }
}