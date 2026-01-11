package com.buildingbadd.demojc.uiscreen.faculty

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentSubmissionsScreen(
    navController: NavHostController
) {

    val db = FirebaseFirestore.getInstance()

    val assignmentId =
        navController.currentBackStackEntry
            ?.arguments
            ?.getString("assignmentId") ?: return

    var submissions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val snapshot = db.collection("assignment_submissions")
            .whereEqualTo("assignmentId", assignmentId)
            .get()
            .await()

        submissions = snapshot.documents.map { it.data!! }
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
                    items(submissions) { submission ->
                        SubmissionCard(submission)
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(submission: Map<String, Any>) {

    val context = LocalContext.current

    val enrollmentId = submission["enrollmentId"] as String
    val studentName = submission["studentName"] as String

    val fileUrl = submission["fileUrl"] as String
    val submittedAt = submission["submittedAt"] as Long

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Enrollment ID: $enrollmentId")
            Text("Name : $studentName")


            Text(
                "Submitted at: ${
                    java.text.SimpleDateFormat("dd MMM yyyy HH:mm")
                        .format(java.util.Date(submittedAt))
                }"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                context.startActivity(intent)
            }) {
                Text("Download Submission")
            }
        }
    }
}

@Preview
@Composable
fun AssignmentSubmissionsScreenPreview() {
    AssignmentSubmissionsScreen(navController = NavHostController(LocalContext.current))
}