package com.example.serviceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.serviceapp.ui.screens.WelcomeScreen
import com.example.serviceapp.ui.screens.AuthScreen
import com.example.serviceapp.ui.screens.CustomerHomeScreen
import com.example.serviceapp.ui.screens.WorkerHomeScreen
import com.example.serviceapp.firebase.FirebaseRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get FCM token and store in Firestore user doc when signed in
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Save token to Firestore for the current user (if signed in). This requires auth state.
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val uid = com.google.firebase.auth.ktx.auth.currentUser?.uid
                        if (uid != null) {
                            val repo = FirebaseRepository()
                            repo.saveFcmToken(uid, token)
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        setContent {
            ServiceApp()
        }
    }
}

@Composable
fun ServiceApp() {
    val navController = rememberNavController()
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = "welcome") {
                composable("welcome") { WelcomeScreen(onContinue = { role -> navController.navigate("auth?role=$role") }) }
                composable("auth?role={role}") { backStackEntry ->
                    val role = backStackEntry.arguments?.getString("role") ?: "customer"
                    AuthScreen(role = role, onSignedIn = { navController.navigate(if (role == "worker") "worker" else "customer") })
                }
                composable("customer") { CustomerHomeScreen() }
                composable("worker") { WorkerHomeScreen(navController) }
                composable("orderDetail/{orderId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("orderId") ?: ""
                    com.example.serviceapp.ui.screens.OrderDetailScreen(id)
                }
            }
        }
    }
}