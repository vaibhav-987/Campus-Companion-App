package com.buildingbadd.demojc.uiscreen.faculty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyProfileScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var facultyId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var assignedClass by remember { mutableStateOf("") }
    var subjects by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // 1️⃣ Get facultyId from users
            val userDoc = db.collection("users").document(uid).get().await()
            facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

            // 2️⃣ Get faculty details
            val facultyDoc =
                db.collection("faculty_details").document(facultyId).get().await()

            name = facultyDoc.getString("name") ?: ""
            department = facultyDoc.getString("department") ?: ""
            assignedClass = facultyDoc.getString("assignedClasses") ?: ""

            val subjectIds = facultyDoc.get("subjects") as? List<String> ?: emptyList()

            // 3️⃣ Fetch subject names
            val subjectList = mutableListOf<Pair<String, String>>()
            for (id in subjectIds) {
                val subjectDoc = db.collection("subjects").document(id).get().await()
                val subjectName = subjectDoc.getString("subjectName") ?: id
                subjectList.add(id to subjectName)
            }

            subjects = subjectList

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Profile") })
        },
        bottomBar = {
            FacultyBottomNavBar(navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {

                FacultyProfileHeader(name, facultyId)

                Spacer(modifier = Modifier.height(20.dp))

                FacultyQuickStats(department, assignedClass, subjects.size)

                Spacer(modifier = Modifier.height(24.dp))

                FacultySubjectsSection(subjects)
            }
        }
    }
}


@Composable
fun FacultyProfileHeader(name: String, facultyId: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            IconButton(
                onClick = { /* upload photo later */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(name, style = MaterialTheme.typography.titleLarge)
        Text("Faculty ID: $facultyId", color = Color.Gray)
    }
}


@Composable
fun FacultyQuickStats(department: String, assignedClass: String, subjectCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStatItem("Department", department)
            QuickStatItem("Class", assignedClass)
            QuickStatItem("Subjects", subjectCount.toString())
        }
    }
}

@Composable
fun QuickStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FacultySubjectsSection(subjects: List<Pair<String, String>>) {

    Column {

        Text(
            text = "Subjects Assigned",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        subjects.forEach { (subjectId, subjectName) ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        text = subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Code: $subjectId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


