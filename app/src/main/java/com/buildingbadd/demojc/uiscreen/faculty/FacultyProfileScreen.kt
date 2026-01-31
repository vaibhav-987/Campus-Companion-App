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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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

    var profile by remember { mutableStateOf<FacultyProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect
        val email = userDoc.getString("email") ?: ""

        val facultyDoc =
            db.collection("faculty_details").document(facultyId).get().await()

        val assignedClasses =
            facultyDoc.get("assignedClasses") as? List<String> ?: emptyList()

        val subjectIds =
            facultyDoc.get("assignedSubjectIds") as? List<String> ?: emptyList()

        val subjects = mutableListOf<Pair<String, String>>()
        for (id in subjectIds) {
            val subjectDoc = db.collection("subjects").document(id).get().await()
            subjects.add(
                id to (subjectDoc.getString("name") ?: id)
            )
        }

        profile = FacultyProfile(
            facultyId = facultyId,
            name = facultyDoc.getString("name") ?: "",
            department = facultyDoc.getString("department") ?: "",
            email = email,
            assignedClasses = assignedClasses,
            subjects = subjects
        )

        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Profile") }) },
        bottomBar = { FacultyBottomNavBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                profile != null -> FacultyProfileContent(profile!!)
            }
        }
    }
}

data class FacultyProfile(
    val facultyId: String,
    val name: String,
    val department: String,
    val email: String,
    val assignedClasses: List<String>,
    val subjects: List<Pair<String, String>>
)

@Composable
fun FacultyProfileContent(profile: FacultyProfile) {

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        FacultyProfileHeader(profile)

        FacultyStatsRow(
            classesCount = profile.assignedClasses.size,
            subjectsCount = profile.subjects.size
        )

        FacultyInfoCard(profile)

        FacultySubjectsSection(profile.subjects)
    }
}

@Composable
fun FacultyProfileHeader(profile: FacultyProfile) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp), // ✅ extra breathing space
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Avatar
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.name,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Faculty ID: ${profile.facultyId}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = profile.department,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
fun FacultyStatsRow(classesCount: Int, subjectsCount: Int) {

    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(classesCount.toString(), "Classes")
            StatItem(subjectsCount.toString(), "Subjects")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, color = Color.Gray)
    }
}

@Composable
fun FacultyInfoCard(profile: FacultyProfile) {

    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(16.dp)) {

            Text("Faculty Information", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            InfoRow("Department", profile.department)
            InfoRow("Email", profile.email)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value)
    }
}

@Composable
fun FacultySubjectsSection(subjects: List<Pair<String, String>>) {

    Column {
        Text(
            "Subjects Assigned",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        subjects.forEach { (id, name) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text("Code: $id", color = Color.Gray)
                }
            }
        }
    }
}