package com.buildingbadd.demojc.uiscreen.faculty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/* ---------------- UI MODEL ---------------- */

data class AttendanceSummaryUI(
    val attendanceId: String,
    val subjectName: String,
    val className: String,
    val date: String,
    val total: Int,
    val present: Int,
    val absent: Int
)

/* ---------------- SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSummaryScreen(
    navController: NavHostController
) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var summaries by remember { mutableStateOf<List<AttendanceSummaryUI>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // get facultyId
            val userDoc = db.collection("users").document(uid).get().await()
            val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

            val snapshot = db.collection("attendance")
                .whereEqualTo("facultyId", facultyId)
                .get()
                .await()

            val list = mutableListOf<AttendanceSummaryUI>()

            for (doc in snapshot.documents) {

                val records = doc.get("records") as? Map<String, Boolean> ?: continue

                val total = records.size
                val present = records.count { it.value }
                val absent = total - present

                list.add(
                    AttendanceSummaryUI(
                        attendanceId = doc.id,
                        subjectName = doc.getString("subjectId") ?: "",
                        className = doc.getString("class") ?: "",
                        date = doc.getString("date") ?: "",
                        total = total,
                        present = present,
                        absent = absent
                    )
                )
            }

            // latest first
            summaries = list.sortedByDescending { it.date }

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CampusAppBar(title = "Attendance Summary",
                onBackClick = { navController.popBackStack() }
            )

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

                summaries.isEmpty() -> Text(
                    "No attendance records found",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(summaries) { summary ->
                        AttendanceSummaryCard(
                            summary = summary,
                            onClick = {
                                navController.navigate(
                                    "attendance_detail/${summary.attendanceId}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- CARD UI ---------------- */

@Composable
fun AttendanceSummaryCard(
    summary: AttendanceSummaryUI,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Lecture: ${summary.subjectName}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Class: ${summary.className}")
            Text("Date: ${summary.date}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Total Students: ${summary.total}")
            Text("Present: ${summary.present}")
            Text("Absent: ${summary.absent}")
        }
    }
}