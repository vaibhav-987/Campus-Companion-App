package com.buildingbadd.demojc.uiscreen.student

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentDetailScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    var submission by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isSubmissionLoading by remember { mutableStateOf(true) }


    val assignmentId =
        navController.currentBackStackEntry
            ?.arguments
            ?.getString("assignmentId")

    // Get assignmentId from route
    if (assignmentId == null) {
        Text("Invalid assignment")
        Toast.makeText(context, "Invalid assignmentId", Toast.LENGTH_SHORT).show()
        return
    }


//    Log.d("ASSIGNMENT_DEBUG", "assignmentId = $assignmentId")

    var assignment by remember { mutableStateOf<Assignment?>(null) }
    var isLoading by remember { mutableStateOf(true) }


    // Fetch assignment from Firestore
    LaunchedEffect(assignmentId) {

            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            val uid = auth.currentUser?.uid ?: return@LaunchedEffect
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            val submissionId = "${assignmentId}_$enrollmentId"

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

        val doc_sub = db.collection("assignment_submissions")
            .document(submissionId)
            .get()
            .await()


        isLoading = false
        submission = doc_sub.data
        isSubmissionLoading = false
    }

    // File submission state
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
            CampusAppBar(title = "Assignment Details",
                onBackClick = { navController.popBackStack() }
            )

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

                    Divider()

                    when {
                        isSubmissionLoading -> {
                            CircularProgressIndicator()
                        }

                        submission == null -> {
                            Text(
                                text = "Not submitted yet",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        submission!!["status"] == "submitted" -> {
                            Text(
                                text = "Submitted – Pending Evaluation",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        submission!!["status"] == "evaluated" -> {
                            val marks = submission!!["marks"]
                            val remarks = submission!!["remarks"] as? String

                            Text(
                                text = "Marks: $marks",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (!remarks.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Remarks: $remarks")
                            }
                        }
                    }


                    // DOWNLOAD (WORKING)
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




                    if (submission == null || submission!!["status"] != "evaluated") {


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