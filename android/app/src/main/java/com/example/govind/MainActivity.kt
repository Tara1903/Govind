package com.example.govind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.govind.data.local.SessionManager
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.navigation.AppState
import com.example.govind.ui.navigation.GovindExperience
import com.example.govind.ui.navigation.MainAppScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Restore experience
        try {
            val exp = GovindExperience.valueOf(sessionManager.lastExperience)
            AppState.switchExperience(exp)
        } catch (e: Exception) {
            AppState.switchExperience(GovindExperience.FRESH)
        }
        
        // Persist experience changes
        AppState.currentExperience.onEach { exp ->
            sessionManager.lastExperience = exp.name
        }.launchIn(lifecycleScope)

        enableEdgeToEdge()
        setContent {
            GovindTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainAppScreen() } }
        }
    }
}
