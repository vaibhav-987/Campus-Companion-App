package com.buildingbadd.demojc.uiscreen.faculty

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyAttendanceScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var facultyId by remember { mutableStateOf("") }

    var lectures by remember { mutableStateOf<List<LectureUI>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            val userDoc = db.collection("users").document(uid).get().await()

            val fetchedFacultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect
            facultyId = fetchedFacultyId

            val snapshot = db.collection("timetables").get().await()

            val today = LocalDate.now().dayOfWeek.name
            val activeSemesterIds = getActiveSemesterIds()

            val result = mutableListOf<LectureUI>()

            for (doc in snapshot.documents) {

                val semesterId = doc.getString("semesterId") ?: continue
                if (!activeSemesterIds.contains(semesterId)) continue

                val className = doc.getString("class") ?: continue
                val slots =
                    doc.get("slots") as? Map<String, List<Map<String, Any>>> ?: continue

                val todaySlots = slots[today] ?: continue

                todaySlots.forEach { slot ->
                    if (slot["facultyId"] == facultyId) {
                        result.add(
                            LectureUI(
                                timetableId = doc.id,
                                subjectId = slot["subjectId"].toString(),
                                className = className,
                                startTime = slot["startTime"].toString(),
                                endTime = slot["endTime"].toString(),
                                room = slot["room"].toString()
                            )
                        )
                    }
                }
            }

            // ✅ THIS WAS MISSING
            lectures = result.sortedBy { it.startTime }

        } catch (e: Exception) {
            Log.e("FacultyAttendance", "Error: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today’s Lectures") }) }
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

                lectures.isEmpty() -> Text(
                    "No lectures today",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(lectures) { lecture ->
                        LectureCard(lecture) {
                            // Suggested: Include subjectId to make the Attendance record unique
                            navController.navigate("mark_attendance/${lecture.timetableId}/${lecture.className}/${lecture.subjectId}/${lecture.startTime}/$facultyId")
                        }
                    }
                }
            }
        }
    }
}

data class LectureUI(
    val timetableId: String,
    val subjectId: String,
    val className: String,
    val startTime: String,
    val endTime: String,
    val room: String
)

@Composable
fun LectureCard(
    lecture: LectureUI,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                lecture.subjectId,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Class: ${lecture.className}")
            Text("${lecture.startTime} - ${lecture.endTime}")
            Text( lecture.room)
        }
    }
}

fun getActiveSemesterIds(): List<String> {
    val month = LocalDate.now().monthValue
    val isOdd = month in 6..10

    return if (isOdd) {
        listOf(
            "BSCIT_SEM_1", "BSCIT_SEM_3", "BSCIT_SEM_5",
            "BCOM_SEM_1", "BCOM_SEM_3", "BCOM_SEM_5"
        )
    } else {
        listOf(
            "BSCIT_SEM_2", "BSCIT_SEM_4", "BSCIT_SEM_6",
            "BCOM_SEM_2", "BCOM_SEM_4", "BCOM_SEM_6"
        )
    }
}
/*-------------Previous Logic ----------------- */
