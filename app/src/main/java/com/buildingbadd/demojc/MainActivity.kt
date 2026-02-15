package com.buildingbadd.demojc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.buildingbadd.demojc.navigation.AppNavGraph
import com.buildingbadd.demojc.ui.theme.DemoJCTheme
import com.buildingbadd.demojc.uiscreen.common.LoadingScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Firebase.initialize(context = this)

        setContent {
            DemoJCTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(user.uid).get()
                            .addOnSuccessListener { doc ->
                                val role = doc.getString("role")
                                // Make sure these strings match your NavGraph routes exactly
                                startDestination = when (role) {
                                    "student" -> "student_home"
                                    "faculty" -> "faculty_home"
                                    "admin" -> "admin_home"
                                    else -> "welcome"
                                }
                            }
                    } else {
                        startDestination = "welcome"
                    }
                }

                if (startDestination == null) {
                    LoadingScreen()
                } else {
                    // Both arguments now match your function signature
                    AppNavGraph(navController = navController, startDestination = startDestination!!)
                }
            }
        }
    }
}