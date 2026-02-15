package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceOverviewScreen(
    navController: NavHostController
) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var attendanceList by remember {
        mutableStateOf<List<StudentAttendanceSummary>>(emptyList())
    }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Get enrollmentId
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            // Get student details
            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()

            val studentClass = studentDoc.getString("class") ?: return@LaunchedEffect
            val semesterId =
                studentDoc.getString("currentSemesterId") ?: return@LaunchedEffect

            // Get subjects of current semester
            val subjectSnapshot =
                db.collection("subjects")
                    .whereEqualTo("semesterId", semesterId)
                    .get()
                    .await()

            val result = mutableListOf<StudentAttendanceSummary>()

            for (subjectDoc in subjectSnapshot.documents) {

                val subjectId = subjectDoc.id ?: continue
                val subjectName = subjectDoc.getString("name") ?: continue

                // Fetch attendance for subject
                val attendanceSnapshot =
                    db.collection("attendance")
                        .whereEqualTo("class", studentClass)
                        .whereEqualTo("subjectId", subjectId)
                        .get()
                        .await()

                var totalLectures = 0
                var presentLectures = 0

                attendanceSnapshot.documents.forEach { attDoc ->
                    totalLectures++
                    val records =
                        attDoc.get("records") as? Map<*, *> ?: emptyMap<Any, Any>()

                    if (records[enrollmentId] == true) {
                        presentLectures++
                    }
                }

                val percentage =
                    if (totalLectures == 0) 0
                    else (presentLectures * 100) / totalLectures

                result.add(
                    StudentAttendanceSummary(
                        subjectId = subjectId,
                        subjectName = subjectName,
                        present = presentLectures,
                        total = totalLectures,
                        percentage = percentage
                    )
                )
            }

            attendanceList = result

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CampusAppBar(title = "Attendance Overview",
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

                attendanceList.isEmpty() -> Text(
                    "No attendance data available",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(attendanceList) { item ->
                        AttendanceOverviewCard(item) {
                            navController.navigate(
                                "attendance_history/${item.subjectId}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceOverviewCard(
    item: StudentAttendanceSummary,
    onClick: () -> Unit
) {

    val color = when {
        item.percentage >= 75 -> Color(0xFF2E7D32) // green
        item.percentage >= 50 -> Color(0xFFF9A825) // yellow
        else -> Color(0xFFC62828) // red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                item.subjectName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Attendance: ${item.percentage}%",
                color = color,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                "Present: ${item.present} / ${item.total}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

data class StudentAttendanceSummary(
    val subjectId: String,
    val subjectName: String,
    val present: Int,
    val total: Int,
    val percentage: Int
)