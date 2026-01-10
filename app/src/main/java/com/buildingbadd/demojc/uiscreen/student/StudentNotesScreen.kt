package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class NoteItem(
    val title: String,
    val subject: String,
    val type: String, // PDF / PPT
    val uploadedOn: String
)

@Composable
fun StudentNotesScreen() {

    // 🔹 Dummy notes (replace with Firestore later)
    val notes = listOf(
        NoteItem("Unit 1 Introduction", "Computer Networks", "PPT", "12 Mar 2025"),
        NoteItem("Unit 2 Routing", "Computer Networks", "PDF", "18 Mar 2025"),
        NoteItem("ER Diagram Notes", "DBMS", "PDF", "20 Mar 2025"),
        NoteItem("Normalization", "DBMS", "PPT", "22 Mar 2025")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Lecture Notes",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(notes) { note ->
                NoteCard(note)
            }
        }
    }
}

@Composable
fun NoteCard(note: NoteItem) {

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
                text = note.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Subject: ${note.subject}")
            Text("Type: ${note.type}")
            Text("Uploaded on: ${note.uploadedOn}")

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    // Later: open PDF/PPT from Firebase Storage
                }
            ) {
                Text("View")
            }
        }
    }
}
