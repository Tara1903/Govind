package com.example.govind

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.govind.data.local.SessionManager
import com.example.govind.data.repository.DeviceTokenRepository
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.navigation.AppState
import com.example.govind.ui.navigation.GovindExperience
import com.example.govind.ui.navigation.MainAppScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var deviceTokenRepository: DeviceTokenRepository

    // Pending navigation route from notification tap
    private val _pendingRoute = mutableStateOf<String?>(null)

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

        // Register FCM token
        lifecycleScope.launch {
            try {
                deviceTokenRepository.registerCurrentToken()
            } catch (e: Exception) {
                Log.w("MainActivity", "FCM token registration failed: ${e.message}")
            }
        }

        // Handle notification tap from launch intent
        _pendingRoute.value = extractRouteFromIntent(intent)

        enableEdgeToEdge()
        setContent {
            val pendingRoute by _pendingRoute
            GovindTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAppScreen(
                        initialRoute = pendingRoute,
                        onRouteConsumed = { _pendingRoute.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val route = extractRouteFromIntent(intent)
        if (route != null) {
            _pendingRoute.value = route
        }
    }

    private fun extractRouteFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        
        // Check URI data (govind:// scheme)
        val data: Uri? = intent.data
        if (data != null && data.scheme == "govind") {
            return when {
                data.host == "order" -> {
                    val orderId = data.pathSegments.firstOrNull() ?: return null
                    val isTracking = data.pathSegments.getOrNull(1) == "tracking"
                    if (isTracking) "order_details/$orderId" else "order_details/$orderId"
                }
                data.host == "cart" -> "cart"
                data.host == "notifications" -> "notifications"
                data.host == "product" -> {
                    val productId = data.pathSegments.firstOrNull() ?: return null
                    "product/$productId"
                }
                else -> null
            }
        }

        // Check intent extras (fallback)
        return intent.getStringExtra("route")
    }
}
