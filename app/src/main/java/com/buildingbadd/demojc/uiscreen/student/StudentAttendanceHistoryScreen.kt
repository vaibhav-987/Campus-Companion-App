package com.buildingbadd.demojc.uiscreen.student

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

data class SubjectAttendanceRecord(
    val date: String,
    val time: String,
    val isPresent: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceHistoryScreen(
    navController: NavHostController,
    subjectId: String
) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var records by remember { mutableStateOf<List<SubjectAttendanceRecord>>(emptyList()) }
    var subjectName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()
            val studentClass = studentDoc.getString("class") ?: return@LaunchedEffect

            val snapshot = db.collection("attendance")
                .whereEqualTo("class", studentClass)
                .whereEqualTo("subjectId", subjectId)
                .get()
                .await()

            val list = mutableListOf<SubjectAttendanceRecord>()

            for (doc in snapshot.documents) {
                val recordsMap = doc.get("records") as? Map<*, *> ?: continue
                val isPresent = recordsMap[enrollmentId] as? Boolean ?: false

                subjectName = doc.getString("subjectName") ?: ""

                list.add(
                    SubjectAttendanceRecord(
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        isPresent = isPresent
                    )
                )
            }

            records = list.sortedWith(
                compareByDescending<SubjectAttendanceRecord> { it.date }
                    .thenByDescending { it.time }
            )

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(subjectName.ifEmpty { "Attendance History" }) })
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

                records.isEmpty() -> {
                    Text("No attendance history found")
                }

                else -> {
                    LazyColumn {
                        items(records) { record ->
                            AttendanceHistoryRow(record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceHistoryRow(record: SubjectAttendanceRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("${record.date}  ${record.time}")
        }

        Text(
            if (record.isPresent) "Present" else "Absent",
            color = if (record.isPresent)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}
