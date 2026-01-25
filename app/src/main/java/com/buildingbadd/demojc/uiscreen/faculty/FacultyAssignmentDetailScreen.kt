package com.buildingbadd.demojc.uiscreen.faculty

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyAssignmentDetailScreen(
    navController: NavHostController,
    assignmentId: String
) {

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var assignment by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(assignmentId) {
        try {
            val doc = db.collection("assignments")
                .document(assignmentId)
                .get()
                .await()

            assignment = doc.data
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
        return
    }

    if (assignment == null) {
        Text("Assignment not found")
        return
    }

    val title = assignment!!["title"] as? String ?: ""
    val description = assignment!!["description"] as? String ?: ""
    val subject = assignment!!["subjectName"] as? String ?: ""
    val className = assignment!!["class"] as? String ?: ""
    val dueDate = assignment!!["dueDate"] as? String ?: ""
    val attachmentUrl = assignment!!["attachmentUrl"] as? String
    val attachmentName = assignment!!["attachmentName"] as? String

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignment Details") },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Assignment"
                        )
                    }
                }
            )
        }
    ) { padding ->


        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text("Class: $className")
            Text("Subject: $subject")
            Text("Due Date: $dueDate")

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Description", style = MaterialTheme.typography.titleMedium)
            Text(description)

            if (attachmentUrl != null) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(attachmentUrl)
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Text(
                        text = "Download ${attachmentName ?: "File"}",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }

    /* ---------------- DELETE CONFIRMATION ---------------- */

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Assignment") },
            text = {
                Text("Are you sure you want to delete this assignment?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            db.collection("assignments")
                                .document(assignmentId)
                                .delete()
                                .await()

                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
