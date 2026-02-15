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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
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
    var showLogoutDialog by remember { mutableStateOf(false) }


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
            phone = facultyDoc.getString("phone") ?: "",
            dob = facultyDoc.getString("dob") ?: "",
            assignedClasses = assignedClasses,
            subjects = subjects
        )

        isLoading = false
    }

    Scaffold(
        topBar = {
            CampusAppBar(
                title = "My Profile",
                actions = {
                    TextButton(onClick = { showLogoutDialog = true }) {
                        Text("Logout")
                    }
                }
            )
        },
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
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    auth.signOut()
                    navController.navigate("welcome") { popUpTo(0) }
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") }
        )
    }
}

data class FacultyProfile(
    val facultyId: String,
    val name: String,
    val department: String,
    val email: String,
    val phone: String = "",
    val dob: String = "",
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

        FacultyInfoCard(profile)

        FacultySubjectsSection(profile.subjects)
    }
}

@Composable
fun FacultyProfileHeader(profile: FacultyProfile) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
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
fun FacultyInfoCard(profile: FacultyProfile) {

    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Faculty Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(icon = Icons.Default.Business, label = "Department", value = profile.department)
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

            InfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email)
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

            InfoRow(icon = Icons.Default.Cake, label = "Date of Birth", value = profile.dob)
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

            InfoRow(icon = Icons.Default.Phone, label = "Phone", value = profile.phone)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
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