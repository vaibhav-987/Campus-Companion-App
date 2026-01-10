package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AssignmentItem(
    val title: String,
    val subject: String,
    val dueDate: String,
    val status: String // Pending / Submitted / Late
)

@Composable
fun StudentAssignmentsScreen() {

    // 🔹 Dummy data (replace with Firestore later)
    val assignments = listOf(
        AssignmentItem("Assignment 1", "Computer Networks", "25 Mar 2025", "Pending"),
        AssignmentItem("Assignment 2", "DBMS", "20 Mar 2025", "Submitted"),
        AssignmentItem("Assignment 3", "Operating Systems", "18 Mar 2025", "Late")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Assignments",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(assignments) { assignment ->
                AssignmentCard(assignment)
            }
        }
    }
}

@Composable
fun AssignmentCard(assignment: AssignmentItem) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Subject: ${assignment.subject}")
            Text("Due Date: ${assignment.dueDate}")
            Text("Status: ${assignment.status}")

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    // Later: submit assignment / view details
                }
            ) {
                Text(
                    if (assignment.status == "Submitted") "View"
                    else "Submit"
                )
            }
        }
    }
}
