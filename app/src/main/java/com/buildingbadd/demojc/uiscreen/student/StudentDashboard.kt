package com.buildingbadd.demojc.uiscreen.student

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.R
import com.buildingbadd.demojc.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val context = LocalContext.current
    var userName by remember { mutableStateOf("Student") }
    var userRole by remember { mutableStateOf("student") }

    LaunchedEffect(Unit) {

        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users")
            .document(uid)
            .get()
            .await()

        userRole = userDoc.getString("role") ?: "student"

        val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

        val studentDoc = db.collection("students_detail")
            .document(enrollmentId)
            .get()
            .await()

        userName = studentDoc.getString("name") ?: "Student"
    }
    val dashboardItems = listOf(
        DashboardItem(
            title = "Attendance",
            iconRes = R.drawable.ic_attendance,
            onClick = { navController.navigate(Routes.STUDENT_ATTENDANCE_OVERVIEW) }
        ),
        DashboardItem(
            title = "Notes",
            iconRes = R.drawable.ic_notes,
            onClick = { navController.navigate("student_subjects") }
        ),
        DashboardItem(
            title = "Assignments",
            iconRes = R.drawable.ic_assignment,
            onClick = { navController.navigate("student_assignments") }
        ),
        DashboardItem(
            title = "Schedules",
            iconRes = R.drawable.ic_presentation,
            onClick = {navController.navigate("student_lectures") }
        )
//        DashboardItem(
//            title = "Feedback",
//            iconRes = R.drawable.ic_feedback,
//            onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() }
//        ),
//        DashboardItem(
//            title = "My Courses",
//            iconRes = R.drawable.ic_courses,
//            onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() }
//        ),
//        DashboardItem(
//            title = "Library",
//            iconRes = R.drawable.ic_library,
//            onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() }
//        ),
//        DashboardItem(
//            title = "Test",
//            iconRes = R.drawable.ic_test,
//            onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() }
//        ),
//
//        DashboardItem(
//            title = "Announcements",
//            iconRes = R.drawable.ic_announcement,
//            onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() }
//        ),
//        DashboardItem(
//            title = "Resources",
//            iconRes = R.drawable.ic_resources,
//            onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() }
//        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hi, $userName",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                ,
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()

                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            StudentBottomNavBar(navController)
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dashboardItems) { item ->
                DashboardGridItem(item)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun StudentPreview(){
    StudentDashboard(navController = NavHostController(context = LocalContext.current))
}

data class DashboardItem(
    val title: String,
    val iconRes: Int,
    val onClick: () -> Unit
)

@Composable
fun DashboardGridItem(item: DashboardItem) {

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { item.onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    modifier = Modifier.size(56.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                   // .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}