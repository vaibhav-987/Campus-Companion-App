package com.buildingbadd.demojc.uiscreen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentDetailsScreen(
    navController: NavHostController,
    enrollmentId: String
) {

    val db = FirebaseFirestore.getInstance()

    var student by remember { mutableStateOf<AdminStudentDetailsUI?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        // Check if registered
        val detailDoc = db.collection("students_detail")
            .document(enrollmentId)
            .get()
            .await()

        if (detailDoc.exists()) {

            student = AdminStudentDetailsUI(
                enrollmentId = enrollmentId,
                name = detailDoc.getString("name") ?: "",
                courseId = detailDoc.getString("course") ?: "",
                className = detailDoc.getString("class") ?: "",
                yearOrSemester = detailDoc.getString("currentSemesterId") ?: "",
                phone = detailDoc.getString("phone") ?: "",
                email = detailDoc.getString("email") ?: "",
                gender = detailDoc.getString("gender") ?: "",
                address = detailDoc.getString("address") ?: "",
                profilePhotoUrl = detailDoc.getString("profilePhotoUrl") ?: "",
                isRegistered = true
            )

        } else {

            // Fetch from master
            val masterDoc = db.collection("students_master")
                .document(enrollmentId)
                .get()
                .await()

            student = AdminStudentDetailsUI(
                enrollmentId = enrollmentId,
                name = masterDoc.getString("name") ?: "",
                courseId = masterDoc.getString("course") ?: "",
                yearOrSemester = masterDoc.getString("year") ?: "",
                phone = masterDoc.getString("phone") ?: "",
                isRegistered = false
            )
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Student Details") })
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

                student != null -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        StudentInfoCard(student!!)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentInfoCard(student: AdminStudentDetailsUI) {

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // HEADER SECTION
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {

                if (student.profilePhotoUrl.isNotBlank()) {

                    AsyncImage(
                        model = student.profilePhotoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )

                } else {

                    Text(
                        text = student.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = student.name,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Enrollment: ${student.enrollmentId}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (student.isRegistered) "Registered"
                        else "Not Registered"
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor =
                        if (student.isRegistered)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                )
            )
        }

        // QUICK STATS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (student.isRegistered) student.className else student.courseId,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(if (student.isRegistered) "Class" else "Course", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = student.yearOrSemester,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (student.isRegistered) "Semester" else "Year",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // DETAILS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Student Information",
                    style = MaterialTheme.typography.titleMedium
                )

                Divider()

                InfoRowModern("Phone", student.phone)

                if (student.isRegistered) {
                    InfoRowModern("Email", student.email)
                    InfoRowModern("Gender", student.gender)
                    InfoRowModern("Address", student.address)
                }
            }
        }
    }
}

@Composable
fun InfoRowModern(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifEmpty { "-" }, fontWeight = FontWeight.Medium)
    }
}

data class AdminStudentDetailsUI(
    val enrollmentId: String = "",
    val name: String = "",
    val courseId: String = "",
    val className: String = "",
    val yearOrSemester: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val address: String = "",
    val profilePhotoUrl: String = "",
    val isRegistered: Boolean = false
)