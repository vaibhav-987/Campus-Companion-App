package com.buildingbadd.demojc.uiscreen.student

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSubjectsScreen(navController: NavHostController) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var subjects by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // 🔹 Get enrollmentId
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            // 🔹 Get student details
            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()

            val currentSemesterId =
                studentDoc.getString("currentSemesterId") ?: return@LaunchedEffect

            // 🔹 Fetch subjects for current semester
            val snapshot = db.collection("subjects")
                .whereEqualTo("semesterId", currentSemesterId)
                .get()
                .await()

            subjects = snapshot.documents.mapNotNull {
                val id = it.getString("subjectID")
                val name = it.getString("name")
                if (id != null && name != null) id to name else null
            }

        } finally {
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Subjects") })
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

                subjects.isEmpty() -> Text(
                    "No subjects available",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(subjects) { subject ->
                        SubjectCard(
                            subjectName = subject.second,
                            onClick = {
                                navController.navigate(
                                    "student_notes/${subject.first}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subjectName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Text(
            text = subjectName,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
