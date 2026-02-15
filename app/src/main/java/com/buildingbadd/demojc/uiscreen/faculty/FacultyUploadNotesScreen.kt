package com.buildingbadd.demojc.uiscreen.faculty

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.navigation.Routes
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyUploadNotesScreen(navController: NavHostController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // ---------------- UI STATE ----------------
    var title by remember { mutableStateOf("") }

    var subjects by remember { mutableStateOf<List<FacultySubjectUI>>(emptyList()) }
    var selectedSubject by remember { mutableStateOf<FacultySubjectUI?>(null) }
    var subjectExpanded by remember { mutableStateOf(false) }

    var selectedFileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var fileNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // ---------------- LOAD SUBJECTS ----------------
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val facultyDoc =
            db.collection("faculty_details").document(facultyId).get().await()

        val assignedSubjectIds =
            facultyDoc.get("assignedSubjectIds") as? List<String> ?: emptyList()

        if (assignedSubjectIds.isEmpty()) return@LaunchedEffect

        val isOdd = currentSemesterType() == SemesterType.ODD
        val filtered = mutableListOf<FacultySubjectUI>()

        for (subjectId in assignedSubjectIds) {
            val subjectDoc =
                db.collection("subjects").document(subjectId).get().await()

            if (!subjectDoc.exists()) continue

            val semesterId = subjectDoc.getString("semesterId") ?: continue
            val semesterNo = semesterId.takeLast(1).toInt()

            val valid =
                if (isOdd) semesterNo % 2 == 1 else semesterNo % 2 == 0

            if (!valid) continue

            filtered.add(
                FacultySubjectUI(
                    subjectId = subjectId,
                    subjectName = subjectDoc.getString("name") ?: "",
                    courseId = subjectDoc.getString("courseId") ?: "",
                    semesterId = semesterId
                )
            )
        }

        subjects = filtered
    }

    // ---------------- FILE PICKER ----------------
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedFileUris = uris
                fileNames = uris.map { getFileName(context, it) }
                validationError = null
            }
        }

    Scaffold(
        topBar = {
            CampusAppBar(
                title = "Upload Notes",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.FACULTY_NOTES_HISTORY) }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Ensures long lists don't hide the button
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (isUploading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubject?.displayText ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Subject") },
                    enabled = !isUploading,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subjectExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = validationError == "Please select a subject"
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.displayText) },
                            onClick = {
                                selectedSubject = subject
                                subjectExpanded = false
                                validationError = null
                            }
                        )
                    }
                }
            }

            // --- 2. TITLE FIELD ---
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; validationError = null },
                label = { Text("Notes Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationError == "Please enter a title"
            )

            // --- 3. MULTI-FILE ATTACH BUTTON ---
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (fileNames.isEmpty()) "Attach Files (PDF/PPT)" else "Change Files")
            }

            // --- 4. SELECTED FILES LIST ---
            if (fileNames.isNotEmpty()) {
                Text("Files to upload:", style = MaterialTheme.typography.labelMedium)
                fileNames.forEach { name ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = name,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 5. INLINE VALIDATION ---
            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        selectedSubject == null -> validationError = "Please select a subject"
                        title.isBlank() -> validationError = "Please enter a title"
                        selectedFileUris.isEmpty() -> validationError = "Please attach at least one file"
                        else -> {
                            isUploading = true
                            var uploadCount = 0

                            selectedFileUris.forEachIndexed { index, uri ->
                                uploadNotes(
                                    subject = selectedSubject!!,
                                    title = title,
                                    fileUri = uri,
                                    fileName = fileNames[index],
                                    onComplete = {
                                        uploadCount++
                                        if (uploadCount == selectedFileUris.size) {
                                            isUploading = false
                                            Toast.makeText(context, "All notes uploaded!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Uploading..." else "Upload All Notes")
            }
        }
    }
}


fun uploadNotes(
    subject: FacultySubjectUI,
    title: String,
    fileUri: Uri,
    fileName: String,
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
        val facultyId = userDoc.getString("facultyId") ?: ""

        val timestamp = System.currentTimeMillis()
        val storageRef = storage.reference.child("notes/${subject.subjectId}/$timestamp" + "_" + "$fileName")

        storageRef.putFile(fileUri)
            .continueWithTask { task -> storageRef.downloadUrl }
            .addOnSuccessListener { url ->
                val data = hashMapOf(
                    "courseId" to subject.courseId,
                    "facultyId" to facultyId,
                    "fileName" to fileName,
                    "fileUrl" to url.toString(),
                    "semesterId" to subject.semesterId,
                    "status" to "active",
                    "subjectId" to subject.subjectId,
                    "subjectName" to subject.subjectName,
                    "title" to title,
                    "uploadedAt" to timestamp
                )

                db.collection("notes")
                    .add(data)
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { onComplete() }
            }
            .addOnFailureListener { onComplete() }
    }
}


enum class SemesterType { ODD, EVEN }

fun currentSemesterType(): SemesterType {
    val month = LocalDate.now().monthValue
    return when (month) {
        in 7..12 -> SemesterType.ODD
        in 1..5 -> SemesterType.EVEN
        else -> throw IllegalStateException("Academic transition month")
    }
}

fun getFileName(context: android.content.Context, uri: Uri): String {
    var name = "Unknown_File"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) name = it.getString(nameIndex)
        }
    }
    return name
}