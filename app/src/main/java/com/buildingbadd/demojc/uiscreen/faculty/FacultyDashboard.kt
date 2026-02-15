package com.buildingbadd.demojc.uiscreen.faculty

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.R
import com.buildingbadd.demojc.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyDashboard(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var facultyName by remember { mutableStateOf("Faculty") }
    var isLoading by remember { mutableStateOf(true) }
    var userRole by remember { mutableStateOf("faculty") }


    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val facultyId = userDoc.getString("facultyId") ?: return@LaunchedEffect

        val facultyDoc =
            db.collection("faculty_details").document(facultyId).get().await()

        facultyName = facultyDoc.getString("name") ?: "Faculty"
        isLoading = false

        userRole = userDoc.getString("role") ?: "faculty"

        val db = FirebaseFirestore.getInstance()

        val doc = db.collection("weekly_timetables")
            .document("BSCIT_SEM1_WEEKLY")
            .get()
            .await()

        Log.d("TIMETABLE_TEST", doc.data.toString())

    }

    Scaffold(
        topBar = {
            androidx.compose.material3.Surface(
                shadowElevation = 4.dp,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                            Text(
                                text = "Hi, $facultyName",
                                style = MaterialTheme.typography.titleLarge
                            )

                    }
                )
            }
        },
        bottomBar = {
            FacultyBottomNavBar(navController)
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                val dashboardItems = listOf(
                    FacultyDashboardItem(
                        title = "Take Attendance",
                        iconRes = R.drawable.ic_take_attendance,
                        route = "faculty_attendance"
                    ),FacultyDashboardItem(
                        title = "Attendance Summary",
                        iconRes = R.drawable.ic_attendance_summary,
                        route = "attendance_summary"
                    ),FacultyDashboardItem(
                        title = "Create Assignment",
                        iconRes = R.drawable.ic_create_assignment,
                        route = "faculty_create_assignment"
                    ),FacultyDashboardItem(
                        title = "Upload Notes",
                        iconRes = R.drawable.ic_upload_notes,
                        route = "faculty_upload_notes"
                    ),FacultyDashboardItem(
                        title = "Check Assignments",
                        iconRes = R.drawable.ic_check_assignment,
                        route = Routes.FACULTY_ASSIGNMENTS_GIVEN
                    ),FacultyDashboardItem(
                        title = "My Assignments",
                        iconRes = R.drawable.ic_my_assignments,
                        route = Routes.FACULTY_ASSIGNMENTS
                    )

                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dashboardItems) { item ->
                        FacultyDashboardCard(item) {
                            navController.navigate(item.route)
                        }
                    }
                }
            }
        }
    }
}

data class FacultyDashboardItem(
    val title: String,
    val iconRes: Int,
    val route: String
)

@Composable
fun FacultyDashboardCard(
    item: FacultyDashboardItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Clean look for light mode
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Display the SVG Icon
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Display the Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}