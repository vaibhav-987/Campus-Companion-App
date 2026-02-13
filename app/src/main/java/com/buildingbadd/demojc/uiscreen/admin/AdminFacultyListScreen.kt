package com.buildingbadd.demojc.uiscreen.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFacultyListScreen(navController: NavHostController) {

    val db = FirebaseFirestore.getInstance()

    var facultyList by remember { mutableStateOf<List<AdminFacultyUI>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        val snapshot = db.collection("faculty_details")
            .get()
            .await()

        facultyList = snapshot.documents.map {
            AdminFacultyUI(
                facultyId = it.id,
                name = it.getString("name") ?: "",
                department = it.getString("department") ?: "",
                subjectsCount = (it.get("assignedSubjectIds") as? List<*>)?.size ?: 0
            )
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Faculty") })
        },
        bottomBar = {
            AdminBottomNavBar(navController)
        }
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {
//                    navController.navigate("admin_add_faculty")
//                }
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add Faculty")
//            }
//        }
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

                facultyList.isEmpty() ->
                    Text(
                        "No faculty added yet",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else ->
                    LazyColumn {
                        items(facultyList) { faculty ->
                            FacultyCard(faculty) {
                                navController.navigate(
                                    "admin_faculty_details/${faculty.facultyId}"
                                )
                            }
                        }
                    }
            }
        }
    }
}

data class AdminFacultyUI(
    val facultyId: String,
    val name: String,
    val department: String,
    val subjectsCount: Int
)

@Composable
fun FacultyCard(
    faculty: AdminFacultyUI,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = faculty.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Faculty ID: ${faculty.facultyId}")
            Text("Department: ${faculty.department}")

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subjects: ${faculty.subjectsCount}")
            }
        }
    }
}