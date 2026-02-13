package com.buildingbadd.demojc.uiscreen.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.DropdownField
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddStudentScreen(navController: NavHostController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var enrollmentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var selectedCourse by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val courses = listOf("BSCIT", "BCOM")
    val years = listOf("FY", "SY", "TY")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Student") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = enrollmentId,
                onValueChange = { enrollmentId = it },
                label = { Text("Enrollment ID") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            DropdownField(
                label = "Select Course",
                options = courses,
                selectedValue = selectedCourse,
                onSelected = { selectedCourse = it }
            )

            DropdownField(
                label = "Select Year",
                options = years,
                selectedValue = selectedYear,
                onSelected = { selectedYear = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    if (
                        enrollmentId.isBlank() ||
                        name.isBlank() ||
                        phone.isBlank() ||
                        selectedCourse.isBlank() ||
                        selectedYear.isBlank()
                    ) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true

                    db.collection("students_master")
                        .document(enrollmentId)
                        .get()
                        .addOnSuccessListener { document ->

                            if (document.exists()) {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Student with this Enrollment ID already exists",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {

                                val data = hashMapOf(
                                    "enrollmentId" to enrollmentId,
                                    "name" to name,
                                    "phone" to phone,
                                    "course" to selectedCourse,
                                    "year" to selectedYear,
                                    "isRegistered" to false,
                                    "createdAt" to System.currentTimeMillis()
                                )

                                db.collection("students_master")
                                    .document(enrollmentId)
                                    .set(data)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Student Added Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Failed to add student",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Something went wrong",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Adding..." else "Add Student")
            }
        }
    }
}