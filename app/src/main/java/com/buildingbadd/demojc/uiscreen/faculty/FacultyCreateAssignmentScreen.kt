package com.buildingbadd.demojc.uiscreen.faculty

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyCreateAssignmentScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf("") }

    var subjects by remember { mutableStateOf<List<FacultySubjectUI>>(emptyList()) }
    var selectedSubject by remember { mutableStateOf<FacultySubjectUI?>(null) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    var facultyId by remember { mutableStateOf("") }
//    var facultyClass by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // ---------------- LOAD FACULTY SUBJECTS ----------------
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val facultyDoc =
            db.collection("faculty_details").document(facultyId).get().await()

//        facultyClass = facultyDoc.getString("class") ?: ""

        val assignedSubjectIds =
            facultyDoc.get("assignedSubjectIds") as? List<String> ?: emptyList()

        if (assignedSubjectIds.isEmpty()) return@LaunchedEffect

        val isOddSemester = isOddSemester()
        val filteredSubjects = mutableListOf<FacultySubjectUI>()

        for (subjectId in assignedSubjectIds) {

            val subjectDoc = db.collection("subjects")
                .document(subjectId)
                .get()
                .await()

            if (!subjectDoc.exists()) continue

            val semesterId = subjectDoc.getString("semesterId") ?: continue
            val semesterNo = semesterId.takeLast(1).toInt()

            val validSemester =
                if (isOddSemester) semesterNo % 2 == 1 else semesterNo % 2 == 0

            if (!validSemester) continue
            val courseId = subjectDoc.getString("courseId") ?: ""

            filteredSubjects.add(
                FacultySubjectUI(
                    subjectId = subjectId,
                    subjectName = subjectDoc.getString("name") ?: "",
                    semesterId = semesterId,
                    courseId = courseId
                )
            )
        }

        subjects = filteredSubjects
        Log.d("ASSIGN", "Filtered subjects = ${subjects.size}")
    }

    // ---------------- FILE PICKER ----------------
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedFileUri = uri
                attachmentName = uri.lastPathSegment ?: "assignment_file"
            }
        }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create Assignment") }) },
        bottomBar = { FacultyBottomNavBar(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ---------------- SUBJECT DROPDOWN ----------------
            ExposedDropdownMenuBox(
                expanded = subjectDropdownExpanded,
                onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubject?.displayText ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Subject") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(subjectDropdownExpanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = subjectDropdownExpanded,
                    onDismissRequest = { subjectDropdownExpanded = false }
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.displayText) },
                            onClick = {
                                selectedSubject = subject
                                subjectDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Assignment Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (attachmentName.isEmpty()) "Attach Assignment File"
                else "Attached: $attachmentName")
            }

            OutlinedTextField(
                value = dueDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                    }
                }
            )

            OutlinedTextField(
                value = totalMarks,
                onValueChange = { totalMarks = it },
                label = { Text("Total Marks") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (selectedSubject == null || title.isBlank() || dueDate.isBlank()) return@Button
                    isLoading = true

                    val subject = selectedSubject!!
                    val subjectId = subject.subjectId
                    val subjectName = subject.subjectName
                    val semesterId = subject.semesterId

                    val courseId = if (semesterId.startsWith("BSCIT")) "BSCIT" else "BCOM"

                    val className = when {
                        semesterId.endsWith("1") || semesterId.endsWith("2") ->
                            if (courseId == "BSCIT") "FYBSCIT" else "FYBCOM"

                        semesterId.endsWith("3") || semesterId.endsWith("4") ->
                            if (courseId == "BSCIT") "SYBSCIT" else "SYBCOM"

                        else ->
                            if (courseId == "BSCIT") "TYBSCIT" else "TYBCOM"
                    }

                    val uploadAndSave: (String?, String?) -> Unit = { name, url ->
                        saveAssignment(
                            db = db,
                            title = title,
                            description = description,
                            subjectId = subjectId,
                            subjectName = subjectName,
                            facultyId = facultyId,
                            courseId = courseId,
                            semesterId = semesterId,
                            className = className,
                            dueDate = dueDate,
                            totalMarks = totalMarks,
                            attachmentName = name,
                            attachmentUrl = url,
                            navController = navController,
                            onComplete = { isLoading = false }
                        )
                    }

                    if (selectedFileUri != null) {
                        val ref = FirebaseStorage.getInstance()
                            .reference.child("assignments/$subjectId/$attachmentName")

                        ref.putFile(selectedFileUri!!)
                            .continueWithTask { ref.downloadUrl }
                            .addOnSuccessListener { uploadAndSave(attachmentName, it.toString()) }
                    } else uploadAndSave(null, null)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Publishing..." else "Publish Assignment")
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }
}

// ---------------- HELPERS ----------------
fun isOddSemester(): Boolean {
    val month = LocalDate.now().monthValue
    return month in 6..10
}



// ---------- FIRESTORE SAVE ----------
fun saveAssignment(
    db: FirebaseFirestore,
    title: String,
    description: String,
    subjectId: String,
    subjectName: String,
    facultyId: String,
    courseId: String,
    semesterId: String,
    className: String,
    dueDate: String,
    totalMarks: String,
    attachmentName: String?,
    attachmentUrl: String?,
    navController: NavHostController,
    onComplete: () -> Unit
) {

    val data = hashMapOf(
        "title" to title,
        "description" to description,
        "courseId" to courseId,
        "semesterId" to semesterId,
        "class" to className,
        "subjectId" to subjectId,
        "subjectName" to subjectName,
        "facultyId" to facultyId,
        "dueDate" to dueDate,
        "totalMarks" to totalMarks.toIntOrNull(),
        "allowLateSubmission" to false,
        "status" to "active",
        "createdAt" to System.currentTimeMillis()
    )

    if (attachmentName != null && attachmentUrl != null) {
        data["attachmentName"] = attachmentName
        data["attachmentUrl"] = attachmentUrl
    }

    db.collection("assignments")
        .add(data)
        .addOnSuccessListener {
            onComplete()
            navController.popBackStack()
        }
        .addOnFailureListener {
            onComplete()
        }
}

// ---------- UI MODEL ----------
