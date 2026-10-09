package com.taxiparis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxiparis.app.ui.MainViewModel
import com.taxiparis.app.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            // Bloquer le logo de démarrage tant que les préférences ne sont pas chargées
            splashScreen.setKeepOnScreenCondition {
                !uiState.isLoaded
            }
            
            if (uiState.isLoaded) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
