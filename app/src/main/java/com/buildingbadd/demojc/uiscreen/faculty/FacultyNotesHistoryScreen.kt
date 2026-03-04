package com.buildingbadd.demojc.uiscreen.faculty

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyNotesHistoryScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var notes by remember { mutableStateOf<List<GroupedNote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val snapshot = db.collection("notes")
            .whereEqualTo("facultyId", facultyId)
            .get()
            .await()

        val fetchedNotes = snapshot.documents.map {
            FacultyNote(
                title = it.getString("title") ?: "",
                subjectName = it.getString("subjectName") ?: "",
                fileUrl = it.getString("fileUrl") ?: "",
                semester = it.getString("semesterId") ?: "",
                uploadedAt = it.getLong("uploadedAt") ?: 0L
            )
        }.sortedBy { it.uploadedAt }

        // ... inside your map/groupBy logic
        val grouped = fetchedNotes.groupBy { it.title + it.subjectName }
            .map { (_, notesInGroup) ->
                val first = notesInGroup.first()
                GroupedNote(
                    title = first.title,
                    subjectName = first.subjectName,
                    semester = first.semester,
                    // The first item is the most recent due to the sort above
                    latestDate = first.uploadedAt,
                    files = notesInGroup.map {
                        NoteFile(
                            name = getFileNameFromUrl(it.fileUrl),
                            url = it.fileUrl,
                            uploadedAt = it.uploadedAt
                        )
                    }
                )
            }.sortedByDescending { it.latestDate }

        notes = grouped

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
                        FacultyNoteHistoryCard(
                            group = note,
                            db = db,
                            onDeleteSuccess= {refreshTrigger++})
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyNoteHistoryCard(
    group: GroupedNote,
    db: FirebaseFirestore,
    onDeleteSuccess: () -> Unit // Fixed parameter
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Define the scope here
    val formattedDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(group.latestDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${group.subjectName} • Sem - ${group.semester}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Attachments",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // File list with delete action
            group.files.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📄 ${file.name}",
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.url))
                                context.startActivity(intent)
                            },
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    androidx.compose.material3.IconButton(
                        onClick = {
                            scope.launch {
                                deleteNoteFile(db, file.url, group) {
                                    android.widget.Toast.makeText(context, "Deleted", android.widget.Toast.LENGTH_SHORT).show()
                                    onDeleteSuccess()
                                }
                            }
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
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

data class GroupedNote(
    val title: String,
    val subjectName: String,
    val semester: String,
    val files: List<NoteFile>,
    val latestDate: Long
)

data class NoteFile(
    val name: String,
    val url: String,
    val uploadedAt: Long
)

fun getFileNameFromUrl(url: String): String {
    return try {
        val decodedUrl = java.net.URLDecoder.decode(url, "UTF-8")

        val lastSlash = decodedUrl.lastIndexOf("/")
        var fileName = if (lastSlash != -1) {
            decodedUrl.substring(lastSlash + 1)
        } else {
            decodedUrl
        }

        val questionMark = fileName.indexOf("?")
        if (questionMark != -1) {
            fileName = fileName.substring(0, questionMark)
        }


        fileName.replace(Regex("^\\d+[_-]"), "")

    } catch (e: Exception) {
        "Attachment"
    }
}

suspend fun deleteNoteFile(
    db: FirebaseFirestore,
    fileUrl: String,
    group: GroupedNote,
    onSuccess: () -> Unit
) {
    try {
        // 1. Delete from Firebase Storage
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
        storageRef.delete().await()

        // 2. Delete the specific document from Firestore
        // We find the doc by matching the fileUrl
        val snapshot = db.collection("notes")
            .whereEqualTo("fileUrl", fileUrl)
            .get()
            .await()

        for (doc in snapshot.documents) {
            db.collection("notes").document(doc.id).delete().await()
        }

        onSuccess()
    } catch (e: Exception) {
        Log.e("DeleteError", "Failed to delete: ${e.message}")
    }
}