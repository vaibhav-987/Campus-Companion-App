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
        mutableStateOf<List<StudentAssignmentUI>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()
            val studentClass = studentDoc.getString("class") ?: return@LaunchedEffect

            val snapshot = db.collection("assignments")
                .whereEqualTo("class", studentClass)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val list = mutableListOf<StudentAssignmentUI>()

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

                val isSubmitted =
                    db.collection("assignment_submissions")
                        .document(submissionDocId)
                        .get()
                        .await()
                        .exists()

                list.add(
                    StudentAssignmentUI(
                        assignment = assignment,
                        isSubmitted = isSubmitted
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
    item: StudentAssignmentUI,
    onClick: () -> Unit
) {
    val assignment = item.assignment

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (item.isSubmitted) "Submitted" else "Pending",
                    color = if (item.isSubmitted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Subject: ${assignment.subjectName}")
            Text("Due Date: ${assignment.dueDate}")
        }
    }
}



data class StudentAssignmentUI(
    val assignment: Assignment,
    val isSubmitted: Boolean
)



