package com.buildingbadd.demojc.uiscreen.faculty

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
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


    Scaffold(
        topBar = {
            CampusAppBar(
                title = "Assignment Details",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Gap between buttons
                ) {
                    Button(
                        onClick = {
                            if (attachmentUrl != null) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachmentUrl))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f), // Takes 50% width
                        enabled = attachmentUrl != null // Disabled if no file exists
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download", maxLines = 1)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f), // Takes 50% width
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error // Makes it Red
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", maxLines = 1)
                    }
                }
            }
        }
    ) { padding ->


        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Text("Title : "+title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Class: $className")
            Text("Subject: $subject")
            Text("Due Date: $dueDate")

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Description", style = MaterialTheme.typography.titleMedium)
            Text(description)

        }
    }

    /* ---------------- DELETE CONFIRMATION ---------------- */

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Assignment") },
            text = {
                Text("Are you sure?")
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
