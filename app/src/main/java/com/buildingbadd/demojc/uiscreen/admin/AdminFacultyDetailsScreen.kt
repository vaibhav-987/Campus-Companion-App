package com.buildingbadd.demojc.uiscreen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFacultyDetailsScreen(
    navController: NavHostController,
    facultyId: String
) {

    val db = FirebaseFirestore.getInstance()

    var profile by remember { mutableStateOf<AdminFacultyDetailUI?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        try {

            // 1 - Get faculty_details
            val facultyDoc =
                db.collection("faculty_details")
                    .document(facultyId)
                    .get()
                    .await()

            if (!facultyDoc.exists()) {
                isLoading = false
                return@LaunchedEffect
            }

            val name = facultyDoc.getString("name") ?: ""
            val department = facultyDoc.getString("department") ?: ""
            val dob = facultyDoc.getString("dob") ?: ""
            val phone = facultyDoc.getString("phone") ?: ""
            val assignedClasses =
                facultyDoc.get("assignedClasses") as? List<String> ?: emptyList()
            val subjectIds =
                facultyDoc.get("assignedSubjectIds") as? List<String> ?: emptyList()
            val active = facultyDoc.getBoolean("active") ?: true

            // 2 - Get email from users
            val userSnapshot = db.collection("users")
                .whereEqualTo("facultyId", facultyId)
                .get()
                .await()

            val email =
                userSnapshot.documents.firstOrNull()?.getString("email") ?: ""

            // 3 - Resolve subject names
            val subjectList = mutableListOf<Triple<String, String, String>>()
            val activeSemesterSet = mutableSetOf<String>()

            val settingsDoc = db.collection("settings")
                .document("current_semester")
                .get()
                .await()

            val currentType = settingsDoc.getString("type") ?: "ODD"

            for (id in subjectIds) {

                val subjectDoc =
                    db.collection("subjects")
                        .document(id)
                        .get()
                        .await()

                if (!subjectDoc.exists()) continue

                val name = subjectDoc.getString("name") ?: id
                val semesterId = subjectDoc.getString("semesterId") ?: ""

                subjectList.add(
                    Triple(id, name, semesterId)
                )

                val semesterDoc =
                    db.collection("semesters")
                        .document(semesterId)
                        .get()
                        .await()

                val semesterType = semesterDoc.getString("type") ?: ""

                if (semesterType == currentType) {
                    activeSemesterSet.add(semesterId)
                }
            }


            profile = AdminFacultyDetailUI(
                facultyId = facultyId,
                name = name,
                department = department,
                email = email,
                dob = dob,
                phone = phone,
                assignedClasses = assignedClasses,
                subjects = subjectList,
                active = active,
                activeSemesterCount = activeSemesterSet.size
            )

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faculty Details") }
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                isLoading ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )

                profile != null ->
                    FacultyDetailsContent(profile!!)
            }
        }
    }
}

data class AdminFacultyDetailUI(
    val facultyId: String,
    val name: String,
    val department: String,
    val email: String,
    val dob: String,
    val phone: String,
    val assignedClasses: List<String>,
    val subjects: List<Triple<String, String, String>>,
    val active: Boolean,
    val activeSemesterCount: Int
)

@Composable
fun FacultyDetailsContent(profile: AdminFacultyDetailUI) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        // PROFILE HEADER
        item {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "Faculty ID: ${profile.facultyId}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (profile.active) "Active" else "Inactive"
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (profile.active)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
        }

        // QUICK STATS CARD
        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            profile.subjects.size.toString(),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Subjects", color = Color.Gray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            profile.activeSemesterCount.toString(),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Active Semesters", color = Color.Gray)
                    }
                }
            }
        }
        // INFORMATION CARD
        item {
            ModernInfoCard(
                title = "Faculty Information",
                items = listOf(
                    "Department" to profile.department,
                    "Email" to profile.email,
                    "Date of Birth" to profile.dob,
                    "Phone" to profile.phone,
                )
            )
        }

        // ASSIGNED SUBJECTS
        item {
            ModernSubjectCard(
                subjects = profile.subjects
            )
        }
    }
}

@Composable
fun ModernInfoCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color.Gray)
                    Text(value.ifEmpty { "-" })
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ModernSubjectCard(
    subjects: List<Triple<String, String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = "Assigned Subjects",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isEmpty()) {

                Text(
                    text = "No subjects assigned",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                subjects.forEach { (id, name, semesterId) ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Code: $id",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}