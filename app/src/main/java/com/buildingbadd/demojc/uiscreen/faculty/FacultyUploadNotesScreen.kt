package com.buildingbadd.demojc.uiscreen.faculty

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/* ---------------------------------------------------
   MAIN SCREEN
--------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyUploadNotesScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    // 🔹 Dynamic data from Firestore
    var classList by remember { mutableStateOf<List<String>>(emptyList()) }
    var subjectList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // 🔹 Form fields
    var title by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }

    /* ---------------------------------------------------
       FETCH FACULTY ASSIGNED CLASSES & SUBJECTS
    --------------------------------------------------- */

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // 1️⃣ Get facultyId
            val userDoc = db.collection("users").document(uid).get().await()
            val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

            // 2️⃣ Get faculty_details
            val facultyDoc =
                db.collection("faculty_details").document(facultyId).get().await()

            /* ---------- CLASSES ---------- */
            val assignedClassesAny = facultyDoc.get("assignedClasses")
            classList = when (assignedClassesAny) {
                is String -> listOf(assignedClassesAny)
                is List<*> -> assignedClassesAny.filterIsInstance<String>()
                else -> emptyList()
            }

            /* ---------- SUBJECTS ---------- */
            /* ---------- SUBJECTS ---------- */

            val subjectsAny = facultyDoc.get("subjects")

            val subjectIds = when (subjectsAny) {
                is List<*> -> subjectsAny.filterIsInstance<String>()
                is String -> listOf(subjectsAny)
                else -> emptyList()
            }

            val resolvedSubjects = mutableListOf<Pair<String, String>>()

            for (subjectId in subjectIds) {
                val subjectDoc =
                    db.collection("subjects").document(subjectId).get().await()

                val subjectName = subjectDoc.getString("subjectName")

                if (subjectName != null) {
                    resolvedSubjects.add(subjectId to subjectName)
                }
            }

            subjectList = resolvedSubjects


        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load faculty data", Toast.LENGTH_LONG).show()
        }
    }

    /* ---------------------------------------------------
       FILE PICKER
    --------------------------------------------------- */

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                selectedFileUri = uri
                selectedFileName = uri.lastPathSegment ?: "notes_file"
            }
        }

    /* ---------------------------------------------------
       UI
    --------------------------------------------------- */

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upload Notes") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notes Title") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownField(
                label = "Select Class",
                options = classList,
                selectedValue = selectedClass,
                onSelected = { selectedClass = it }
            )

            DropdownField(
                label = "Select Subject",
                options = subjectList.map { it.second },
                selectedValue = selectedSubjectName,
                onSelected = { name ->
                    val subject = subjectList.first { it.second == name }
                    selectedSubjectId = subject.first
                    selectedSubjectName = subject.second
                }
            )

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (selectedFileName.isEmpty())
                        "Choose File (PDF / PPT)"
                    else
                        "Selected: $selectedFileName"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (
                        title.isBlank() ||
                        selectedClass.isBlank() ||
                        selectedSubjectId.isBlank() ||
                        selectedFileUri == null
                    ) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true

                    scope.launch {
                        try {
                            uploadNotes(
                                title = title,
                                className = selectedClass,
                                subjectId = selectedSubjectId,
                                subjectName = selectedSubjectName,
                                fileUri = selectedFileUri!!,
                                fileName = selectedFileName
                            )
                            Toast.makeText(context, "Notes uploaded successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "Upload failed", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Uploading..." else "Upload Notes")
            }
        }
    }
}

/* ---------------------------------------------------
   FIREBASE UPLOAD LOGIC
--------------------------------------------------- */

suspend fun uploadNotes(
    title: String,
    className: String,
    subjectId: String,
    subjectName: String,
    fileUri: Uri,
    fileName: String
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    val uid = auth.currentUser!!.uid
    val userDoc = db.collection("users").document(uid).get().await()
    val facultyId = userDoc.getString("facultyId")!!

    val storageRef =
        storage.reference.child("notes/$className/$subjectId/$fileName")

    storageRef.putFile(fileUri).await()
    val downloadUrl = storageRef.downloadUrl.await()

    val data = hashMapOf(
        "title" to title,
        "class" to className,
        "subjectId" to subjectId,
        "subjectName" to subjectName,
        "facultyId" to facultyId,
        "fileName" to fileName,
        "fileUrl" to downloadUrl.toString(),
        "uploadedAt" to System.currentTimeMillis()
    )

    db.collection("notes").add(data).await()
}

/* ---------------------------------------------------
   DROPDOWN COMPONENT (Material3 SAFE)
--------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
