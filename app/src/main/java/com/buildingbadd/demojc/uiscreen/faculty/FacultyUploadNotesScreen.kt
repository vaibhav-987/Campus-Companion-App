package com.buildingbadd.demojc.uiscreen.faculty

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/* ---------------------------------------------------
   FACULTY UPLOAD NOTES (FINAL – SEMESTER AWARE)
--------------------------------------------------- */

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

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

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
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedFileUri = uri
                fileName = uri.lastPathSegment ?: "notes_file"
            }
        }

    // ---------------- UI ----------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Notes") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.FACULTY_NOTES_HISTORY) }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Notes History"
                        )
                    }
                },
            )
        },
        bottomBar = { FacultyBottomNavBar(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // SUBJECT DROPDOWN
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubject?.displayText ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Subject") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(subjectExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notes Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (fileName.isEmpty()) "Attach PDF / PPT" else fileName)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (
                        selectedSubject == null ||
                        title.isBlank() ||
                        selectedFileUri == null
                    ) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true

                    uploadNotes(
                        subject = selectedSubject!!,
                        title = title,
                        fileUri = selectedFileUri!!,
                        fileName = fileName,
                        navController = navController,
                        onComplete = {
                            isUploading = false
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Uploading..." else "Upload Notes")
            }
        }
    }
}


fun uploadNotes(
    subject: FacultySubjectUI,
    title: String,
    fileUri: Uri,
    fileName: String,
    navController: NavHostController,
    onComplete: () -> Unit
) {

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    val uid = auth.currentUser?.uid ?: return

    // 1️⃣ Fetch facultyId safely
    db.collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { userDoc ->

            if (!userDoc.exists()) {
                onComplete()
                return@addOnSuccessListener
            }

            val facultyId = userDoc.getString("facultyId")

            if (facultyId.isNullOrBlank()) {
                onComplete()
                return@addOnSuccessListener
            }

            // 2️⃣ Upload file
            val ref = storage.reference
                .child("notes/${subject.subjectId}/$fileName")

            ref.putFile(fileUri)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { url ->

                    val data = hashMapOf(
                        "title" to title,
                        "courseId" to subject.courseId,
                        "semesterId" to subject.semesterId,
                        "subjectId" to subject.subjectId,
                        "subjectName" to subject.subjectName,
                        "facultyId" to facultyId, // ✅ REAL facultyId
                        "fileName" to fileName,
                        "fileUrl" to url.toString(),
                        "uploadedAt" to System.currentTimeMillis(),
                        "status" to "active"
                    )

                    db.collection("notes")
                        .add(data)
                        .addOnSuccessListener {
                            onComplete()
                            navController.popBackStack()
                        }
                }
        }
        .addOnFailureListener {
            onComplete()
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


