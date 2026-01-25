package com.buildingbadd.demojc.uiscreen.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentsScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var assignments by remember {
        mutableStateOf<List<StudentAssignmentItem>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            val studentDoc = db.collection("students_detail")
                .document(enrollmentId)
                .get()
                .await()

            val currentSemesterId =
                studentDoc.getString("currentSemesterId") ?: return@LaunchedEffect
            val studentClass = studentDoc.getString("class") ?: return@LaunchedEffect

            val snapshot = db.collection("assignments")
                .whereEqualTo("semesterId", currentSemesterId)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val list = mutableListOf<StudentAssignmentItem>()

            for (doc in snapshot.documents) {
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

                val submissionDocId = "${assignment.id}_$enrollmentId"

                val submissionDoc =
                    db.collection("assignment_submissions")
                        .document(submissionDocId)
                        .get()
                        .await()

                val status = submissionDoc.getString("status")
                val marks = submissionDoc.getLong("marks")?.toInt()

                list.add(
                    StudentAssignmentItem(
                        assignment = assignment,
                        submissionStatus = status,
                        marks = marks
                    )
                )

            }

            assignments = list

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Assignments") })
        },
        bottomBar = {
            StudentBottomNavBar(navController)
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

                assignments.isEmpty() -> {
                    Text(
                        "No assignments available",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn {
                        items(assignments) { item ->
                            AssignmentCard(
                                item = item,
                                onClick = {
                                    navController.navigate(
                                        "${Routes.STUDENT_ASSIGNMENT_DETAIL}/${item.assignment.id}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AssignmentCard(
    item: StudentAssignmentItem,
    onClick: () -> Unit
) {
    val assignment = item.assignment
    val status = item.submissionStatus
    val marks = item.marks

    val statusColor = when (status) {
        "evaluated" -> Color(0xFF2E7D32)   // ✅ green
        "submitted" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Subject: ${assignment.subjectName}")
            Text("Due Date: ${assignment.dueDate}")

            Spacer(modifier = Modifier.height(8.dp))

            when (status) {

                "evaluated" -> {
                    Text(
                        text = "Evaluated${if (marks != null) " • Marks: $marks" else ""}",
                        color = Color(0xFF2E7D32), // green
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                "submitted" -> {
                    Text(
                        text = "Pending Evaluation",
                        color = Color(0xFFF9A825), // amber
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                else -> {
                    Text(
                        text = "Not Submitted",
                        color = MaterialTheme.colorScheme.error, // red
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}





data class StudentAssignmentItem(
    val assignment: Assignment,
    val submissionStatus: String?, // "submitted" | "evaluated" | null
    val marks: Int?
)




