package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var attendanceList by remember { mutableStateOf<List<StudentAttendanceRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid
                ?: throw Exception("User not logged in")

            // 1️⃣ Get enrollmentId
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId")
                ?: throw Exception("Enrollment ID missing")

            // 2️⃣ Get class
            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()
            val studentClass = studentDoc.getString("class")
                ?: throw Exception("Class missing")

            // 3️⃣ Query attendance
            val snapshot = db.collection("attendance")
                .whereEqualTo("class", studentClass)
                .get()
                .await()

            val list = mutableListOf<StudentAttendanceRecord>()

            for (doc in snapshot.documents) {
                val records = doc.get("records") as? Map<*, *> ?: continue

                if (records.containsKey(enrollmentId)) {
                    val isPresent = records[enrollmentId] as Boolean

                    list.add(
                        StudentAttendanceRecord(
                            subjectName = doc.getString("subjectName") ?: "Unknown",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            isPresent = isPresent
                        )
                    )
                }
            }

            attendanceList = list.sortedWith(
                compareByDescending<StudentAttendanceRecord> { it.date }
                    .thenByDescending { it.time }
            )

        } catch (e: Exception) {
            e.printStackTrace()
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
        ) {

            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                attendanceList.isEmpty() -> {
                    Text(
                        "No attendance records found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        items(attendanceList) { record ->
                            AttendanceItemCard(record)
                        }
                    }
                }
            }
        }
    }
}

data class StudentAttendanceRecord(
    val subjectName: String,
    val date: String,
    val time: String,
    val isPresent: Boolean
)

@Composable
fun AttendanceItemCard(record: StudentAttendanceRecord) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = record.subjectName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Date: ${record.date}  |  Time: ${record.time}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (record.isPresent) "Present" else "Absent",
                color = if (record.isPresent)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


