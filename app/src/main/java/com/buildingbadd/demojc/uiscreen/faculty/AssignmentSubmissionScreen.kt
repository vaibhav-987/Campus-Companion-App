package com.buildingbadd.demojc.uiscreen.faculty

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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentSubmissionsScreen(
    navController: NavHostController,
    assignmentId: String
) {

    val db = FirebaseFirestore.getInstance()

    var submissions by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(assignmentId) {
        val snapshot = db.collection("assignment_submissions")
            .whereEqualTo("assignmentId", assignmentId)
            .get()
            .await()

        submissions = snapshot.documents.mapNotNull { doc ->
            doc.data?.let { doc.id to it }
        }

        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Submissions") }) }
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

                submissions.isEmpty() ->
                    Text("No submissions yet", modifier = Modifier.align(Alignment.Center))

                else -> LazyColumn {
                    items(submissions) { (submissionId, submission) ->
                        SubmissionCard(
                            submission = submission,
                            onEvaluate = {
                                navController.navigate(
                                    "evaluate_assignment/$submissionId"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(
    submission: Map<String, Any>,
    onEvaluate: () -> Unit
) {

    val context = LocalContext.current

    val enrollmentId = submission["enrollmentId"] as? String ?: ""
    val studentName = submission["studentName"] as? String ?: ""
    val fileUrl = submission["fileUrl"] as? String ?: ""
    val submittedAt = submission["submittedAt"] as? Long ?: 0L
    val status = submission["status"] as? String ?: "submitted"
    val marks = submission["marks"]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onEvaluate() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Enrollment ID: $enrollmentId")
            Text("Name: $studentName")

            if (submittedAt != 0L) {
                Text(
                    "Submitted at: ${
                        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                            .format(Date(submittedAt))
                    }"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (status == "evaluated")
                    "Marks: $marks"
                else
                    "Pending Evaluation",
                color = if (status == "evaluated")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                    context.startActivity(intent)
                }
            ) {
                Text("Download Submission")
            }
        }
    }
}
