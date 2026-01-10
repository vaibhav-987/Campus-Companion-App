package com.buildingbadd.demojc.uiscreen.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize(),

        contentAlignment = Alignment.Center
    ) {
//        Image(
//            painter = painterResource(id = R.drawable.signin_screen), // <-- Replace with your image name
//            contentDescription = "Background Image",
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.FillBounds // This will crop the image to fill the screen
//        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 45.dp, 10.dp, 0.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent

                )

            )
            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 5.dp, 10.dp, 20.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent

                )

            )
//            Spacer(modifier = Modifier.height(.dp))

            val context = LocalContext.current

            Button(onClick = {
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val user = FirebaseAuth.getInstance().currentUser
                            if (user == null) {
                                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }

                            val uid = user.uid
                            val db = FirebaseFirestore.getInstance()

                            db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener { document ->

                                    if (!document.exists()) {
                                        Toast.makeText(
                                            context,
                                            "User data not found. Please sign up again.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@addOnSuccessListener
                                    }

                                    val role = document.getString("role")
                                    val status = document.getString("status")

                                    if (status == "pending") {
                                        navController.navigate("pending") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    } else if (role == "student") {
                                        navController.navigate("student_home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    } else if (role == "faculty") {
                                        navController.navigate("faculty_home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    } else if (role == "admin") {
                                        navController.navigate("admin_approval") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }

                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Firestore error", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(
                                context,
                                task.exception?.message ?: "Login failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            }, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("signup") {
                    popUpTo("login") {
                        inclusive = true
                    }

                }

            }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}




