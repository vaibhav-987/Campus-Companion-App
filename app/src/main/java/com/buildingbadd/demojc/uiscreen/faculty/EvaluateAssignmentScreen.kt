package com.buildingbadd.demojc.uiscreen.faculty

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluateAssignmentScreen(
    navController: NavHostController,
    submissionId: String
) {

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var submission by remember { mutableStateOf<Map<String, Any>?>(null) }
    var marks by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(submissionId) {
        try {
            val doc = db.collection("assignment_submissions")
                .document(submissionId)
                .get()
                .await()

            submission = doc.data

            marks = submission?.get("marks")?.toString() ?: ""
            remarks = submission?.get("remarks") as? String ?: ""

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Evaluate Assignment") })
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

                submission == null -> Text(
                    "Submission not found",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {

                    val enrollmentId = submission!!["enrollmentId"] as? String ?: ""
                    val fileUrl = submission!!["fileUrl"] as? String ?: ""

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        Text(
                            text = "Enrollment ID: $enrollmentId",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (fileUrl.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(fileUrl)
                                    )
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download Submission")
                            }
                        }

                        Divider()

                        OutlinedTextField(
                            value = marks,
                            onValueChange = { marks = it },
                            label = { Text("Marks") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text("Remarks (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (marks.isBlank()) {
                                    return@Button
                                }

                                isSaving = true

                                scope.launch {
                                    db.collection("assignment_submissions")
                                        .document(submissionId)
                                        .update(
                                            mapOf(
                                                "marks" to marks.toInt(),
                                                "remarks" to remarks,
                                                "status" to "evaluated"
                                            )
                                        )
                                        .await()

                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            Text(if (isSaving) "Saving..." else "Save Evaluation")
                        }
                    }
                }
            }
        }
    }
}
