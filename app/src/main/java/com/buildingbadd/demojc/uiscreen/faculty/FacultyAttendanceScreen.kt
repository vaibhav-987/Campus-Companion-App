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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyAttendanceScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var facultyClass by remember { mutableStateOf<String?>(null) }
    var students by remember { mutableStateOf<List<StudentAttendanceItem>>(emptyList()) }
    val attendanceMap = remember { mutableStateMapOf<String, Boolean>() }
    var isLoading by remember { mutableStateOf(true) }
    var subjects by remember { mutableStateOf<List<SubjectItem>>(emptyList()) }
    var selectedSubject by remember { mutableStateOf<SubjectItem?>(null) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        // 1️⃣ Get facultyId
        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        // 2️⃣ Get faculty class (ONLY ONE)
        val facultyDoc =
            db.collection("faculty_details").document(facultyId).get().await()

        facultyClass = facultyDoc.getString("assignedClasses")

        val subjectIds =
            facultyDoc.get("subjects") as? List<String> ?: emptyList()

        val subjectList = mutableListOf<SubjectItem>()

        for (subId in subjectIds) {
            val subDoc = db.collection("subjects").document(subId).get().await()
            subjectList.add(
                SubjectItem(
                    subjectId = subId,
                    subjectName = subDoc.getString("subjectName") ?: subId
                )
            )
        }

        subjects = subjectList

        facultyClass?.let { cls ->
            db.collection("students_detail")
                .whereEqualTo("class", cls)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.map {
                        StudentAttendanceItem(
                            enrollmentId = it.getString("enrollmentId") ?: "",
                            name = it.getString("name") ?: ""
                        )
                    }

                    students = list
                    attendanceMap.clear()
                    attendanceMap.putAll(list.associate { it.enrollmentId to true })

                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Take Attendance") })
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                students.isEmpty() -> {
                    Text(
                        "No students found for your class",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        Text(
                            text = "Class: $facultyClass",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedSubject == null) {

                            Text(
                                "Select Subject",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            subjects.chunked(2).forEach { rowSubjects ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowSubjects.forEach { subject ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(90.dp)
                                                .clickable {
                                                    selectedSubject = subject
                                                },
                                            elevation = CardDefaults.cardElevation(6.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    subject.subjectName,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                        } else {

                            Text(
                                text = "Subject: ${selectedSubject!!.subjectName}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Class: $facultyClass",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedButton(onClick = { showDatePicker = true }) {
                                        Text("Date: ${selectedDate.format(dateFormatter)}")
                                    }

                                    OutlinedButton(onClick = { showTimePicker = true }) {
                                        Text("Time: ${selectedTime.format(timeFormatter)}")
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    items(students) { student ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clickable {
                                                    val currentValue =
                                                        attendanceMap[student.enrollmentId] ?: true
                                                    attendanceMap[student.enrollmentId] =
                                                        !currentValue
                                                },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = student.enrollmentId,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Checkbox(
                                                checked = attendanceMap[student.enrollmentId]
                                                    ?: true,
                                                onCheckedChange = { newValue ->
                                                    attendanceMap[student.enrollmentId] = newValue
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        selectedSubject?.let { subject ->
                                            saveAttendance(
                                                db = db,
                                                className = facultyClass.toString(),
                                                subject = subject,
                                                date = selectedDate.toString(),
                                                time = selectedTime.toString(),
                                                records = attendanceMap
                                            )
                                            navController.popBackStack()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Attendance")
                                }

                            }
                        }


                    }
                }
            }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }


}

data class StudentAttendanceItem(
    val enrollmentId: String,
    val name: String
)

fun saveAttendance(
    db: FirebaseFirestore,
    className: String,
    subject: SubjectItem,
    date: String,
    time: String,
    records: Map<String, Boolean>
) {
    val docId = "${className}_${subject.subjectId}_${date}_${time}"

    val data = hashMapOf(
        "class" to className,
        "subjectId" to subject.subjectId,
        "subjectName" to subject.subjectName,
        "date" to date,
        "time" to time,
        "records" to records
    )

    db.collection("attendance")
        .document(docId)
        .set(data)
}


data class SubjectItem(
    val subjectId: String,
    val subjectName: String
)
