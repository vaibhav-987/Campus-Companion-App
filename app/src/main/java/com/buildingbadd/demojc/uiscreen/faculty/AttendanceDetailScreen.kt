package com.buildingbadd.demojc.uiscreen.faculty

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
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.buildingbadd.demojc.uiscreen.faculty.components.StudentAttendanceRow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailScreen(
    navController: NavHostController,
    attendanceId: String
) {

    val db = FirebaseFirestore.getInstance()

    var isLoading by remember { mutableStateOf(true) }

    var subjectName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    var students by remember {
        mutableStateOf<List<StudentAttendanceUI>>(emptyList())
    }

    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("attendance")
                .document(attendanceId)
                .get()
                .await()

            subjectName = doc.getString("subjectName") ?: ""
            className = doc.getString("class") ?: ""
            date = doc.getString("date") ?: ""
            time = doc.getString("startTime") ?: ""

            val records = doc.get("records") as? Map<String, Boolean> ?: emptyMap()

            students = records.map {
                StudentAttendanceUI(
                    enrollmentId = it.key,
                    isPresent = it.value
                )
            }.sortedBy { it.enrollmentId }

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CampusAppBar(title = "Attendance Details",
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

                else -> Column {

                    /* ---------- LECTURE INFO ---------- */

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                text = subjectName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Class: $className")
                            Text("Date: $date")
                            if (time.isNotEmpty()) {
                                Text("Time: $time")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    /* ---------- STUDENT LIST ---------- */

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(students) { student ->
                            StudentAttendanceRow(student)
                        }
                    }
                }
            }
        }
    }
}


data class StudentAttendanceUI(
    val enrollmentId: String,
    val isPresent: Boolean
)