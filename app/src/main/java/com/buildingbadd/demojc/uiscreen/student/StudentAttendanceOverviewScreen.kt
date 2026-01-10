package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class SubjectAttendanceSummary(
    val subjectId: String,
    val subjectName: String,
    val attended: Int,
    val total: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceOverviewScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var summaryList by remember { mutableStateOf<List<SubjectAttendanceSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // 1️⃣ Get enrollmentId
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            // 2️⃣ Get class
            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()
            val studentClass = studentDoc.getString("class") ?: return@LaunchedEffect

            // 3️⃣ Fetch attendance for class
            val snapshot = db.collection("attendance")
                .whereEqualTo("class", studentClass)
                .get()
                .await()

            val map = mutableMapOf<String, SubjectAttendanceSummary>()

            for (doc in snapshot.documents) {
                val subjectId = doc.getString("subjectId") ?: continue
                val subjectName = doc.getString("subjectName") ?: "Unknown"
                val records = doc.get("records") as? Map<*, *> ?: continue

                val isPresent = records[enrollmentId] as? Boolean ?: false

                val existing = map[subjectId]

                if (existing == null) {
                    map[subjectId] = SubjectAttendanceSummary(
                        subjectId = subjectId,
                        subjectName = subjectName,
                        attended = if (isPresent) 1 else 0,
                        total = 1
                    )
                } else {
                    map[subjectId] = existing.copy(
                        attended = existing.attended + if (isPresent) 1 else 0,
                        total = existing.total + 1
                    )
                }
            }

            summaryList = map.values.toList()

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Attendance") })
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                isLoading -> CircularProgressIndicator()

                summaryList.isEmpty() -> {
                    Text("No attendance data available")
                }

                else -> {
                    LazyColumn {
                        items(summaryList) { summary ->
                            SubjectAttendanceCard(summary) {
                                navController.navigate(
                                    "attendance_history/${summary.subjectId}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(
    summary: SubjectAttendanceSummary,
    onClick: () -> Unit
) {
    val progress =
        if (summary.total == 0) 0f
        else summary.attended.toFloat() / summary.total.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                summary.subjectName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("${summary.attended} / ${summary.total} lectures")

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
