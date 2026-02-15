package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.uiscreen.common.CampusAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTodayLecturesScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var lectures by remember { mutableStateOf<List<TodayLecture>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var todayName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // Get enrollmentId
            val userDoc = db.collection("users").document(uid).get().await()
            val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

            // Get student semester
            val studentDoc =
                db.collection("students_detail").document(enrollmentId).get().await()

            val semesterId = studentDoc.getString("currentSemesterId") ?: return@LaunchedEffect

            // Get today
            val today = LocalDate.now().dayOfWeek.name
            todayName = today.lowercase().replaceFirstChar { it.uppercase() }

            // Get timetable
            val timetableDoc =
                db.collection("timetables").document(semesterId).get().await()

            val slots = timetableDoc.get("slots") as Map<*, *>
            val todaySlots = slots[today] as? List<Map<String, Any>> ?: emptyList()

            val lectureList = mutableListOf<TodayLecture>()

            for (slot in todaySlots) {

                val subjectId = slot["subjectId"] as String
                val facultyId = slot["facultyId"] as String

                // Fetch subject name
                val subjectDoc =
                    db.collection("subjects").document(subjectId).get().await()
                val subjectName = subjectDoc.getString("name") ?: subjectId

                // Fetch faculty name
                val facultyDoc =
                    db.collection("faculty_details").document(facultyId).get().await()
                val facultyName = facultyDoc.getString("name") ?: facultyId

                lectureList.add(
                    TodayLecture(
                        startTime = slot["startTime"] as String,
                        endTime = slot["endTime"] as String,
                        subjectName = subjectName,
                        facultyName = facultyName,
                        room = slot["room"] as String
                    )
                )
            }

            lectures = lectureList


        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CampusAppBar(title = "Schedule",
                onBackClick = { navController.popBackStack() }
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                lectures.isEmpty() ->
                    Text(
                        "No lectures today 🎉",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> {
                    Column {
                        Text(
                            text = todayName,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn {
                            items(lectures) { lecture ->
                                LectureCard(lecture)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LectureCard(lecture: TodayLecture) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Keeps everything aligned in the middle
        ) {
            // --- LEFT SIDE: LECTURE DETAILS ---
            Column(
                modifier = Modifier.weight(1f) // This pushes the room info to the right
            ) {
                Text(
                    text = "${lecture.startTime} - ${lecture.endTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary // Adds a nice pop of color to the time
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lecture.subjectName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Faculty: ${lecture.facultyName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- RIGHT SIDE: ROOM BADGE ---
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer, // Light blue/grey background
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lecture.room,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

data class TodayLecture(
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val facultyName: String,
    val room: String
)
