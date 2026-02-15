package com.buildingbadd.demojc.uiscreen.student

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNotesBySubjectScreen(
    navController: NavHostController,
    subjectId: String
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var notes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(subjectId) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            val studentDoc = db.collection("students_detail").document(enrollmentId).get().await()
            val currentSemesterId = studentDoc.getString("currentSemesterId") ?: ""

            // Fetch notes filtered by subjectId and semesterId
            val snapshot = db.collection("notes")
                .whereEqualTo("subjectId", subjectId)
                .whereEqualTo("semesterId", currentSemesterId)
                .get()
                .await()

            notes = snapshot.documents.mapNotNull { it.data }
        } finally {
            isLoading = false
        }
    }

    // Groups all notes with the same title into a single list
    val groupedNotes = notes.groupBy { it["title"] as? String ?: "Untitled" }
    val sortedTitles = groupedNotes.keys.toList().sortedByDescending { title ->
        groupedNotes[title]?.firstOrNull()?.get("uploadedAt") as? Long ?: 0L
    }

    Scaffold(
        topBar = {
            CampusAppBar(title = "Notes", onBackClick = { navController.popBackStack() })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                groupedNotes.isEmpty() -> Text("No notes available", modifier = Modifier.align(Alignment.Center))
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedTitles) { title ->
                        val files = groupedNotes[title] ?: emptyList()
                        ExpandableNoteCard(title, files, context)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableNoteCard(title: String, files: List<Map<String, Any>>, context: android.content.Context) {
    var expanded by remember { mutableStateOf(false) }

    // Formatting date from the first file in the group
    val uploadedAt = files.firstOrNull()?.get("uploadedAt") as? Long ?: 0L
    val formattedDate = if (uploadedAt != 0L)
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(uploadedAt))
    else ""

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    if (formattedDate.isNotEmpty()) {
                        Text(text = "Uploaded on: $formattedDate", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = "${files.size} file(s)", style = MaterialTheme.typography.labelSmall)
                }
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                files.forEach { file ->
                    val fileName = file["fileName"] as? String ?: "Attachment"
                    val fileUrl = file["fileUrl"] as? String ?: ""

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                            context.startActivity(intent)
                        }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = fileName, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Download", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}
