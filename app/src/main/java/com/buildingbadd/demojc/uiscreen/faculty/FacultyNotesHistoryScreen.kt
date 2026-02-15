package com.buildingbadd.demojc.uiscreen.faculty

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyNotesHistoryScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var notes by remember { mutableStateOf<List<FacultyNote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val snapshot = db.collection("notes")
            .whereEqualTo("facultyId", facultyId)
            .get()
            .await()

        notes = snapshot.documents.map {
            FacultyNote(
                title = it.getString("title") ?: "",
                subjectName = it.getString("subjectName") ?: "",
                fileUrl = it.getString("fileUrl") ?: "",
                semester = it.getString("semesterId") ?: "",
                uploadedAt = it.getLong("uploadedAt") ?: 0L
            )
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            CampusAppBar(title = "Notes History",
                onBackClick = { navController.popBackStack() }
            )

        }
    )
     { padding ->

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

                notes.isEmpty() -> Text(
                    "No notes uploaded yet",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(notes) { note ->
                        FacultyNoteHistoryCard(note)
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyNoteHistoryCard(note: FacultyNote) {

    val context = LocalContext.current

    val formattedDate =
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(note.uploadedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(note.fileUrl))
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Subject: ${note.subjectName}")
            Text("Semester: ${note.semester}")
            Text(
                text = "Uploaded: $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class FacultyNote(
    val title: String,
    val subjectName: String,
    val fileUrl: String,
    val semester: String,
    val uploadedAt: Long
)