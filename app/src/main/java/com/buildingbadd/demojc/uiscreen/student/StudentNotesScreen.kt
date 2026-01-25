package com.buildingbadd.demojc.uiscreen.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun StudentNotesScreen(navController: NavHostController) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var subjects by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()

            val currentSemesterId =
                studentDoc.getString("currentSemesterId") ?: return@LaunchedEffect

            // 🔹 Fetch subjects for semester
            val snapshot = db.collection("subjects")
                .whereEqualTo("semesterId", currentSemesterId)
                .get()
                .await()

            subjects = snapshot.documents.mapNotNull {
                val id = it.getString("subjectId")
                val name = it.getString("name")
                if (id != null && name != null) id to name else null
            }

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes") }) }
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

                subjects.isEmpty() -> Text(
                    "No subjects found",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(subjects) { (subjectId, subjectName) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate(
                                        "student_notes_subject/$subjectId"
                                    )
                                },
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(subjectName, style = MaterialTheme.typography.titleMedium)
                                Text(subjectId, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentNoteCard(
    note: Map<String, Any>,
    context: android.content.Context
) {
    val title = note["title"] as? String ?: ""
    val subject = note["subjectName"] as? String ?: ""
    val fileUrl = note["fileUrl"] as? String ?: ""
    val uploadedAt = note["uploadedAt"] as? Long ?: 0L

    val formattedDate =
        if (uploadedAt != 0L)
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(uploadedAt))
        else
            ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Subject: $subject")

            if (formattedDate.isNotEmpty()) {
                Text(
                    "Uploaded on: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tap to download",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
