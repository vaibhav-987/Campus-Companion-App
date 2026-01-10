package com.buildingbadd.demojc.uiscreen.auth

import android.content.Context
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupScreen(navController: NavHostController) {

    // 🔹 Form fields
    var enrollmentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }


    // 🔹 Fake OTP state
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Enrollment ID
            OutlinedTextField(
                value = enrollmentId,
                onValueChange = { enrollmentId = it },
                label = { Text("Enrollment ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 7.dp, 10.dp, 0.dp),
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

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 7.dp, 10.dp, 0.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 7.dp, 10.dp, 0.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 7.dp, 10.dp, 0.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 7.dp, 10.dp, 0.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 7.dp, 10.dp, 0.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 SIGN UP BUTTON
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val db = FirebaseFirestore.getInstance()

                    db.collection("students_master")
                        .document(enrollmentId)
                        .get()
                        .addOnSuccessListener { doc ->

                            if (!doc.exists()) {
                                Toast.makeText(context, "Invalid Enrollment ID", Toast.LENGTH_SHORT)
                                    .show()
                                return@addOnSuccessListener
                            }

                            val registeredPhone = doc.getString("phone")
                            val studentClassFromDb = doc.getString("year")

                            if (registeredPhone != phone) {
                                Toast.makeText(
                                    context,
                                    "Phone number does not match records",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnSuccessListener
                            }

                            // 🔥 THIS IS THE PART YOU ASKED ABOUT
                            studentClass = studentClassFromDb ?: ""
                            otpSent = true

                            Toast.makeText(context, "OTP sent (demo)", Toast.LENGTH_SHORT).show()
                        }

                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error checking student record",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }

            // 🔹 FAKE OTP UI
            if (otpSent) {

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("Enter OTP") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (otp == "123456") {
                            createUserAfterFakeOtp(
                                email = email,
                                password = password,
                                enrollmentId = enrollmentId,
                                name = name,
                                phone = phone,
                                studentClass = studentClass,
                                context = context,
                                navController = navController
                            )
                        } else {
                            Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify OTP & Register")
                }
            }
        }
    }
}

fun createUserAfterFakeOtp(
    email: String,
    password: String,
    enrollmentId: String,
    name: String,
    phone: String,
    studentClass: String,
    context: Context,
    navController: NavHostController
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->

            val uid = result.user!!.uid

            // 🔹 1. users collection (auth + role)
            val userData = hashMapOf(
                "uid" to uid,
                "email" to email,
                "role" to "student",
                "enrollmentId" to enrollmentId
            )

            // 🔹 2. students_detail collection (profile)
            val studentDetail = hashMapOf(
                "enrollmentId" to enrollmentId,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "class" to studentClass
            )

            // Save both
            db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener {

                    db.collection("students_detail")
                        .document(enrollmentId) // 👈 use enrollmentId as doc ID
                        .set(studentDetail)
                        .addOnSuccessListener {

                            Toast.makeText(
                                context,
                                "Registration successful",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                }
        }
        .addOnFailureListener {
            Toast.makeText(
                context,
                "Registration failed: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
}

