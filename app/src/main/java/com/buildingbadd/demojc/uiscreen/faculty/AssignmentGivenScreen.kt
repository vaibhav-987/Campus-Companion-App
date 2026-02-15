package com.buildingbadd.demojc.uiscreen.faculty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.navigation.Routes
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.buildingbadd.demojc.uiscreen.student.Assignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentGivenScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var assignments by remember { mutableStateOf<List<AssignmentWithStats>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val assignmentSnapshot = db.collection("assignments")
            .whereEqualTo("facultyId", facultyId)
            .get()
            .await()

        val result = mutableListOf<AssignmentWithStats>()

        for (doc in assignmentSnapshot.documents) {

            val assignment = Assignment(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                subjectId = doc.getString("subjectId") ?: "",
                subjectName = doc.getString("subjectName") ?: "",
                className = doc.getString("class") ?: "",
                dueDate = doc.getString("dueDate") ?: "",
                attachmentName = doc.getString("attachmentName"),
                attachmentUrl = doc.getString("attachmentUrl")
            )

            // 🔹 total students of class
            val totalStudents = db.collection("students_detail")
                .whereEqualTo("class", assignment.className)
                .get()
                .await()
                .size()

            // 🔹 submissions count
            val submittedCount = db.collection("assignment_submissions")
                .whereEqualTo("assignmentId", assignment.id)
                .get()
                .await()
                .size()

            result.add(
                AssignmentWithStats(
                    assignment = assignment,
                    submittedCount = submittedCount,
                    totalStudents = totalStudents
                )
            )
        }

        assignments = result
        isLoading = false
    }
    Scaffold(
        topBar = {
            CampusAppBar(title = "Check Assignment",
                onBackClick = { navController.popBackStack() }
            )

        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                assignments.isEmpty() ->
                    Text(
                        "No assignments created",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> LazyColumn {
                    items(assignments) { item ->
                        val assignment = item.assignment
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate(
                                        "${Routes.FACULTY_ASSIGNMENT_SUBMISSIONS}/${assignment.id}"
                                    )

                                },
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(assignment.title, style = MaterialTheme.typography.titleMedium)
                                Text("Class: ${assignment.className}")
                                Text("Subject: ${assignment.subjectName}")
                                Text("Due: ${assignment.dueDate}")

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${item.submittedCount} / ${item.totalStudents} students submitted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }                        }
                    }
                }
            }
        }
    }
}

data class AssignmentWithStats(
    val assignment: Assignment,
    val submittedCount: Int,
    val totalStudents: Int
)
