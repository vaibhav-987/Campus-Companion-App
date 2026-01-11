package com.buildingbadd.demojc.uiscreen.student

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    val assignmentId =
        navController.currentBackStackEntry
            ?.arguments
            ?.getString("assignmentId")

    // ✅ Get assignmentId from route
    if (assignmentId == null) {
        Text("Invalid assignment")
        Toast.makeText(context, "Invalid assignmentId", Toast.LENGTH_SHORT).show()
        return
    }


//    Log.d("ASSIGNMENT_DEBUG", "assignmentId = $assignmentId")

    var assignment by remember { mutableStateOf<Assignment?>(null) }
    var isLoading by remember { mutableStateOf(true) }


    // 🔹 Fetch assignment from Firestore
    LaunchedEffect(assignmentId) {
        if (assignmentId == null) return@LaunchedEffect

        val doc = db.collection("assignments")
            .document(assignmentId)
            .get()
            .await()

        assignment = Assignment(
            id = doc.id,
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            subjectId = doc.getString("subjectId") ?: "",
            subjectName = doc.getString("subjectName") ?: "",
            className = doc.getString("class") ?: "",
            dueDate = doc.getString("dueDate") ?: "",
            attachmentName = doc.getString("attachmentName"),
            attachmentUrl = doc.getString("attachmentUrl")
        )


        isLoading = false
    }

    // 🔹 File submission state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedFileUri = uri
                selectedFileName = uri.lastPathSegment ?: "submission_file"
            }
        }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Assignment Details") })
        }
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            assignment == null -> {
                Text("Assignment not found")
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(assignment!!.title, style = MaterialTheme.typography.headlineSmall)
                    Text("Subject: ${assignment!!.subjectName}")
                    Text("Due Date: ${assignment!!.dueDate}")

                    Divider()

                    Text("Description", style = MaterialTheme.typography.titleMedium)
                    Text(assignment!!.description)

                    // ✅ DOWNLOAD (WORKING)
                    if (assignment!!.attachmentUrl != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, assignment!!.attachmentUrl!!.toUri())
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download ${assignment!!.attachmentName ?: "File"}")
                        }
                    }

                    Divider()

                    Text("Submit Assignment", style = MaterialTheme.typography.titleMedium)

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (selectedFileName.isEmpty())
                                "Choose File"
                            else
                                "Selected: $selectedFileName"
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedFileUri == null) return@Button

                            isSubmitting = true

                            scope.launch {
                                submitAssignment(
                                    assignment = assignment!!,
                                    fileUri = selectedFileUri!!,
                                    fileName = selectedFileName
                                )
                                isSubmitting = false
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting
                    ) {
                        Text(if (isSubmitting) "Submitting..." else "Submit Assignment")
                    }
                }
            }
        }
    }
}


suspend fun submitAssignment(
    assignment: Assignment,
    fileUri: Uri,
    fileName: String
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val uid = auth.currentUser!!.uid

    val userDoc = db.collection("users").document(uid).get().await()
    val enrollmentId = userDoc.getString("enrollmentId")!!
    val studentDoc = db.collection("students_detail").document(enrollmentId).get().await()
    val studentName = studentDoc.getString("name")

    val storageRef =
        storage.reference
            .child("assignment_submissions/${assignment.id}/${enrollmentId}_$fileName")

    val uploadTask = storageRef.putFile(fileUri).await()
    val downloadUrl = storageRef.downloadUrl.await()

    val docId = "${assignment.id}_$enrollmentId"

    val data = hashMapOf(
        "assignmentId" to assignment.id,
        "enrollmentId" to enrollmentId,
        "class" to assignment.className,
        "subjectId" to assignment.subjectId,
        "studentName" to studentName,
        "fileName" to fileName,
        "fileUrl" to downloadUrl.toString(),
        "submittedAt" to System.currentTimeMillis(),
        "status" to "submitted",
        "marks" to null
    )

    db.collection("assignment_submissions")
        .document(docId)
        .set(data)
        .await()
}
