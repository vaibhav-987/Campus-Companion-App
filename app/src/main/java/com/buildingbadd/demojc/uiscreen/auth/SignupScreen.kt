package com.buildingbadd.demojc.uiscreen.auth

import android.content.Context
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import java.time.LocalDate
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.KeyboardType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
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
    var courseId by remember { mutableStateOf("") }
    var dobAdd by remember { mutableStateOf("")}
    var showDatePicker by remember { mutableStateOf(false)}


    // 🔹 Fake OTP state
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
            .verticalScroll(rememberScrollState()), // Add this!
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(10.dp, 7.dp, 10.dp, 0.dp)
            ) {
                OutlinedTextField(
                    value = dobAdd,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    // enabled = false ensures the click passes through to the Box
                    enabled = false,
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
            }



            // 🔹 Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis
                                if (selectedMillis != null) {
                                    val selectedDate = Instant
                                        .ofEpochMilli(selectedMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()

                                    dobAdd = selectedDate.format(
                                        DateTimeFormatter.ISO_LOCAL_DATE
                                    )
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }


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
                            val courseIdFromDb = doc.getString("course")

                            if (registeredPhone != phone) {
                                Toast.makeText(
                                    context,
                                    "Phone number does not match records",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnSuccessListener
                            }

                            if (dobAdd.isBlank()) {
                                Toast.makeText(context, "Please select Date of Birth", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            studentClass = studentClassFromDb ?: ""
                            courseId = courseIdFromDb ?: ""
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
                                dob = dobAdd,
                                studentClass = studentClass,
                                courseId = courseId,
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
    dob: String,
    studentClass: String,
    courseId: String,
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

            val semesterId = calculateSemesterId(
                courseId = courseId,
                studentClass = studentClass
            )

            // 🔹 2. students_detail collection (profile)
            val studentDetail = hashMapOf(
                "enrollmentId" to enrollmentId,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "dob" to dob,
                "course" to courseId,
                "class" to studentClass,
                "currentSemesterId" to semesterId
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

fun calculateSemesterId(
    courseId: String,
    studentClass: String,
    signupDate: LocalDate = LocalDate.now()
): String {

    val month = signupDate.monthValue
    val day = signupDate.dayOfMonth

    val isSemOdd =
        (month in 6..10) || (month == 11 && day < 15)

    return when (studentClass) {
        "FYBSCIT", "FYBCOM" ->
            if (isSemOdd) "${courseId}_SEM_1" else "${courseId}_SEM_2"

        "SYBSCIT", "SYBCOM" ->
            if (isSemOdd) "${courseId}_SEM_3" else "${courseId}_SEM_4"

        "TYBSCIT", "TYBCOM" ->
            if (isSemOdd) "${courseId}_SEM_5" else "${courseId}_SEM_6"

        else -> ""
    }
}