package com.buildingbadd.demojc.uiscreen.faculty

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyCreateAssignmentScreen(navController: NavHostController) {

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("") }

    var facultyId by remember { mutableStateOf("") }
    var facultyClass by remember { mutableStateOf("") }

    // 🔹 Subject selection (simple dropdown)
    var subjects by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedSubjectId by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    // 🔹 Load faculty details + subjects
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val facultyDoc =
            db.collection("faculty_details").document(facultyId).get().await()

        facultyClass = facultyDoc.getString("assignedClasses") ?: ""

        val subjectIds = facultyDoc.get("subjects") as? List<String> ?: emptyList()

        val subjectList = mutableListOf<Pair<String, String>>()
        for (id in subjectIds) {
            val subjectDoc = db.collection("subjects").document(id).get().await()
            val name = subjectDoc.getString("subjectName") ?: id
            subjectList.add(id to name)
        }


        subjects = subjectList
    }
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedFileUri = uri
                attachmentName = uri.lastPathSegment ?: "assignment_file"
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Assignment") })
        },
        bottomBar = {
            FacultyBottomNavBar(navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 🔹 SUBJECT DROPDOWN
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSubjectName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Subject") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    subjects.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedSubjectId = id
                                selectedSubjectName = name
                                expanded = false
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
                Text(
                    if (attachmentName.isEmpty())
                        "Attach Assignment File"
                    else
                        "Attached: $attachmentName"
                )
            }


            OutlinedTextField(
                value = dueDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                }
            )


            OutlinedTextField(
                value = totalMarks,
                onValueChange = { totalMarks = it },
                label = { Text("Total Marks") },
                modifier = Modifier.fillMaxWidth()
            )




            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (
                        selectedSubjectId.isEmpty() ||
                        title.isEmpty() ||
                        dueDate.isEmpty()
                    ) return@Button

                    isLoading = true

                    if (selectedFileUri != null) {

                        val storageRef = FirebaseStorage.getInstance()
                            .reference
                            .child("assignments/$selectedSubjectId/${attachmentName}")

                        storageRef.putFile(selectedFileUri!!)
                            .continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let { throw it }
                                }
                                storageRef.downloadUrl
                            }
                            .addOnSuccessListener { downloadUrl ->

                                saveAssignmentToFirestore(
                                    db,
                                    title,
                                    description,
                                    selectedSubjectId,
                                    selectedSubjectName,
                                    facultyId,
                                    facultyClass,
                                    dueDate,
                                    totalMarks,
                                    attachmentName,
                                    downloadUrl.toString(),
                                    navController,
                                    onComplete = { isLoading = false }
                                )
                            }

                    } else {
                        // No attachment
                        saveAssignmentToFirestore(
                            db,
                            title,
                            description,
                            selectedSubjectId,
                            selectedSubjectName,
                            facultyId,
                            facultyClass,
                            dueDate,
                            totalMarks,
                            null,
                            null,
                            navController,
                            onComplete = { isLoading = false }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Publishing..." else "Publish Assignment")
            }

        }
    }
    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val localDate = java.time.Instant
                                .ofEpochMilli(selectedMillis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()

                            dueDate = localDate.toString() // YYYY-MM-DD
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}

fun saveAssignmentToFirestore(
    db: FirebaseFirestore,
    title: String,
    description: String,
    subjectId: String,
    subjectName: String,
    facultyId: String,
    facultyClass: String,
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
        "subjectId" to subjectId,
        "subjectName" to subjectName,
        "facultyId" to facultyId,
        "class" to facultyClass,
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

