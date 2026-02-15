package com.buildingbadd.demojc.uiscreen.faculty

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val timetableId =
        navController.currentBackStackEntry?.arguments?.getString("timetableId") ?: return
    val className =
        navController.currentBackStackEntry?.arguments?.getString("className") ?: return
    val subjectId =
        navController.currentBackStackEntry?.arguments?.getString("subjectId") ?: return
    val startTime =
        navController.currentBackStackEntry?.arguments?.getString("startTime") ?: return
    val facultyId = navController.currentBackStackEntry?.arguments?.getString("facultyId") ?: return
    val semId = navController.currentBackStackEntry?.arguments?.getString("semesterId") ?: return

    val today = LocalDate.now().toString()

    var students by remember { mutableStateOf<List<StudentItem>>(emptyList()) }
    val attendanceMap = remember { mutableStateMapOf<String, Boolean>() }
    var isLoading by remember { mutableStateOf(true) }
    var attendanceAlreadyMarked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        // Load students
        val snapshot = db.collection("students_detail")
            .whereEqualTo("class", className)
            .get()
            .await()

        students = snapshot.documents.map {
            StudentItem(
                enrollmentId = it.getString("enrollmentId") ?: "",
                name = it.getString("name") ?: ""
            )
        }

        // Check if attendance already exists
        val attendanceDocId = "${className}_${subjectId}_${today}_$startTime"

        val attendanceDoc =
            db.collection("attendance")
                .document(attendanceDocId)
                .get()
                .await()

        if (attendanceDoc.exists()) {
            attendanceAlreadyMarked = true

            val records =
                attendanceDoc.get("records") as? Map<String, Boolean> ?: emptyMap()

            attendanceMap.clear()
            attendanceMap.putAll(records)
        } else {
            attendanceAlreadyMarked = false
            attendanceMap.clear()
            students.forEach {
                attendanceMap[it.enrollmentId] = true
            }
        }

        isLoading = false
    }
    Scaffold(
        topBar = {
            CampusAppBar(title = "Mark Attendance",
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

            if (attendanceAlreadyMarked) {
                Text(
                    "Attendance already marked for this lecture",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> Column {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(students) { student ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(student.name)
                                    Text(
                                        student.enrollmentId,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Checkbox(
                                    checked = attendanceMap[student.enrollmentId] == true,
                                    onCheckedChange = {
                                        if (!attendanceAlreadyMarked) {
                                            attendanceMap[student.enrollmentId] = it
                                        }
                                    },
                                    enabled = !attendanceAlreadyMarked
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            saveAttendance(
                                db = db,
                                timetableId = timetableId,
                                className = className,
                                subjectId = subjectId,
                                facultyId = facultyId,
                                date = today,
                                startTime = startTime,
                                records = attendanceMap,
                                semesterId = semId
                            )
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !attendanceAlreadyMarked
                    ) {
                        Text(
                            if (attendanceAlreadyMarked)
                                "Attendance Already Marked"
                            else
                                "Save Attendance"
                        )
                    }
                }
            }
        }
    }
}

fun saveAttendance(
    db: FirebaseFirestore,
    timetableId: String,
    className: String,
    subjectId: String,
    facultyId: String,
    date: String,
    startTime: String,
    records: Map<String, Boolean>,
    semesterId: String
) {
    val docId = "${className}_${subjectId}_${date}_$startTime"
    val TAG = "sem ID: "

    val data = hashMapOf(
        "timetableId" to timetableId,
        "class" to className,
        "subjectId" to subjectId,
        "facultyId" to facultyId,
        "date" to date,
        "startTime" to startTime,
        "records" to records,
        "semesterId" to semesterId
    )

    Log.d(TAG,semesterId)

    db.collection("attendance")
        .document(docId)
        .set(data)
}

data class StudentItem(
    val enrollmentId: String,
    val name: String
)