package com.buildingbadd.demojc.uiscreen.auth


import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.R
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun SignupScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center

    ) {
        Image(
            painter = painterResource(id = R.drawable.signup_screen), // <-- Replace with your image name
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds // This will crop the image to fill the screen
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
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

            Row {
                RadioButton(
                    selected = selectedRole == "student",
                    onClick = { selectedRole = "student" }
                )
                Text("Student")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = selectedRole == "faculty",
                    onClick = { selectedRole = "faculty" }
                )
                Text("Faculty")
            }

//            Spacer(modifier = Modifier.height(24.dp))

            val context = LocalContext.current

            Button(onClick = {
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val uid = FirebaseAuth.getInstance().currentUser!!.uid
                            val db = FirebaseFirestore.getInstance()

                            // user data to store
                            val userMap = hashMapOf(
                                "uid" to uid,
                                "email" to email,
                                "role" to selectedRole,
                                "status" to "pending"    // admin will approve later
                            )

                            // save to Firestore
                            db.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        context,
                                        "Sign Up Successful. Waiting for approval.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("login") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Firestore error: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        } else {
                            Toast.makeText(
                                context,
                                task.exception?.message ?: "Sign Up failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign Up")
            }


            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("signup") {
                        inclusive = true
                    }

                }

            }) {
                Text("Already have an account? Login")
            }
        }
    }
}



