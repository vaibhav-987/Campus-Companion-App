package com.buildingbadd.demojc.uiscreen.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.navigation.Routes
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentListScreen(
    navController: NavHostController,
    courseId: String,
    year: String
) {

    val db = FirebaseFirestore.getInstance()

    var students by remember { mutableStateOf<List<AdminStudentUI>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        val masterSnapshot = db.collection("students_master")
            .whereEqualTo("course", courseId)
            .whereEqualTo("year", year)
            .get()
            .await()

        val tempList = mutableListOf<AdminStudentUI>()

        for (doc in masterSnapshot.documents) {

            val enrollmentId = doc.getString("enrollmentId") ?: continue
            val name = doc.getString("name") ?: ""
            val studentYear = doc.getString("year") ?: ""

            // Check if registered
            val detailDoc = db.collection("students_detail")
                .document(enrollmentId)
                .get()
                .await()

            val isRegistered = detailDoc.exists()

            tempList.add(
                AdminStudentUI(
                    enrollmentId = enrollmentId,
                    name = name,
                    year = studentYear,
                    isRegistered = isRegistered
                )
            )
        }

        students = tempList
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$courseId - $year Students") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.ADMIN_ADD_STUDENT)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {

                isLoading ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )

                students.isEmpty() ->
                    Text(
                        "No students found",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else ->
                    LazyColumn {
                        items(students) { student ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        navController.navigate(
                                            "admin_student_details/${student.enrollmentId}"
                                        )
                                    },
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    Text(
                                        student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text("Enrollment: ${student.enrollmentId}")
                                    Text("Year: ${student.year}")

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (student.isRegistered)
                                            "Registered"
                                        else
                                            "Not Registered",
                                        color = if (student.isRegistered)
                                            Color(0xFF2E7D32)
                                        else
                                            Color.Red,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}

data class AdminStudentUI(
    val enrollmentId: String,
    val name: String,
    val year: String,
    val isRegistered: Boolean
)