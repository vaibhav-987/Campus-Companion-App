package com.buildingbadd.demojc.uiscreen.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavHostController) {

    // Form fields
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

    var gender by remember { mutableStateOf("") }

    var enrollmentError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Fake OTP state
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // Enrollment ID
            OutlinedTextField(
                value = enrollmentId,
                onValueChange = {
                    enrollmentId = it
                    enrollmentError = if (it.isBlank()) "Enrollment ID required" else null
                },
                label = { Text("Enrollment ID") },
                isError = enrollmentError != null,
                modifier = Modifier.fillMaxWidth(),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            enrollmentError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (it.isBlank()) "Name required" else null
                },
                label = { Text("Full Name") },
                isError = nameError != null,
                modifier = Modifier.fillMaxWidth()
            )

            nameError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = dobAdd,
                    onValueChange = { },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    isError = dobError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor =
                            if (dobError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            dobError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            GenderDropdown(
                selectedGender = gender,
                onGenderSelected = {
                    gender = it
                    genderError = false
                },
                isError = genderError
            )


            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError =
                        if (it.length != 10 || !it.all { ch -> ch.isDigit() })
                            "Enter valid 10-digit phone number"
                        else null
                },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                modifier = Modifier.fillMaxWidth()
            )

            phoneError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError =
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches())
                            "Invalid email"
                        else null
                },
                label = { Text("Email") },
                isError = emailError != null,
                modifier = Modifier.fillMaxWidth()
            )

            emailError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError =
                        if (it.length < 6 || !it.any { ch -> ch.isDigit() } || !it.any { ch -> ch.isLetter() })
                            "Password must contain at least one letter and one digit"
                        else null
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                modifier = Modifier.fillMaxWidth()
            )

            passwordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError =
                        if (it != password)
                            "Passwords don't match"
                        else null
                },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                modifier = Modifier.fillMaxWidth()
            )

            confirmPasswordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

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


            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }


                    enrollmentError = if (enrollmentId.isBlank()) "Enrollment ID required" else enrollmentError
                    nameError = if (name.isBlank()) "Name required" else nameError
                    phoneError = if (phone.isBlank()) "Enter valid 10-digit phone number" else phoneError
                    dobError = if (dobAdd.isBlank()) "Date of Birth is required" else dobError
                    genderError = gender.isBlank()
                    emailError = if (email.isBlank()) "Enter Valid Email" else emailError
                    passwordError = if (password.isBlank()) "Required" else passwordError
                    confirmPasswordError =
                        if (confirmPassword.isBlank()) "Required" else confirmPasswordError

                    if (
                        enrollmentError != null ||
                        nameError != null ||
                        phoneError != null ||
                        dobError != null ||
                        genderError ||
                        emailError != null ||
                        passwordError != null ||
                        confirmPasswordError != null
                    ) {
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
                                gender = gender,
                                studentClass = studentClass,
                                courseId = courseId,
                                context = context,
                                isActive = true,
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
    gender: String,
    studentClass: String,
    courseId: String,
    context: Context,
    isActive: Boolean,
    navController: NavHostController
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->

            val uid = result.user!!.uid

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

            val studentDetail = hashMapOf(
                "enrollmentId" to enrollmentId,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "dob" to dob,
                "gender" to gender,
                "course" to courseId,
                "class" to studentClass,
                "currentSemesterId" to semesterId,
                "isActive" to isActive
            )

            // Save both
            db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener {

                    db.collection("students_detail")
                        .document(enrollmentId)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    isError: Boolean
) {
    val genderOptions = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,

                unfocusedBorderColor = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.outline
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genderOptions.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    }
                )
            }
        }
    }
}