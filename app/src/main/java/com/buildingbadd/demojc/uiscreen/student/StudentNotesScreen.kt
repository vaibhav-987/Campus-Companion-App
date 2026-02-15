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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentNotesSubjectScreen(navController: NavHostController, subjectId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var notes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(subjectId) {
        try {
            // Fetch only notes belonging to this specific subject
            val snapshot = db.collection("notes")
                .whereEqualTo("subjectId", subjectId)
                .whereEqualTo("status", "active")
                .get()
                .await()

            notes = snapshot.documents.mapNotNull { it.data }
        } finally {
            isLoading = false
        }
    }

    // 2. Grouping Logic: Combine files with the same title
    val groupedNotes = notes.groupBy { it["title"] as? String ?: "Untitled" }

    Scaffold(
        topBar = {
            CampusAppBar(
                title = "Subject Notes",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                groupedNotes.isEmpty() -> Text(
                    "No notes available for this subject",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(groupedNotes.keys.toList()) { title ->
                            val files = groupedNotes[title] ?: emptyList()

                            // Use the Expandable Grouped Card
                            GroupedNoteCard(
                                title = title,
                                files = files,
                                context = context
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupedNoteCard(
    title: String,
    files: List<Map<String, Any>>,
    context: android.content.Context
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Extract date from the first file in the group
    val uploadedAt = files.firstOrNull()?.get("uploadedAt") as? Long ?: 0L
    val formattedDate = if (uploadedAt != 0L) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(uploadedAt))
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(), // Smoothly animate expansion
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER ROW: Title, Date, and Arrow ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (formattedDate.isNotEmpty()) {
                        Text(
                            text = "Uploaded on: $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Show file count to student
                    Text(
                        text = "${files.size} attachment(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- EXPANDABLE ATTACHMENT LIST ---
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                files.forEach { file ->
                    val fileName = file["fileName"] as? String ?: "Attachment"
                    val fileUrl = file["fileUrl"] as? String ?: ""

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (fileUrl.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                                    context.startActivity(intent)
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Download",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}