package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(navController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    TextButton(onClick = { /* Edit later */ }) {
                        Text("Edit")
                    }
                }
            )
        },
        bottomBar = {
            StudentBottomNavBar(navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            ProfileHeaderModern(
                name = "VR",
                enrollmentId = "210520201234",
                studentClass = "FYBSCIT"
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileQuickStats()
            Spacer(modifier = Modifier.height(24.dp))

            InfoCard(
                title = "Personal Information",
                items = listOf(
                    "Date of Birth" to "1 Jan 2002",
                    "Gender" to "Male"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                title = "Contact Details",
                items = listOf(
                    "Phone" to "1234567890",
                    "Email" to "abc@gmail.com",
                    "Address" to "Mumbai, India"
                )
            )
        }
    }
}

@Composable
fun ProfileHeaderModern(
    name: String,
    enrollmentId: String,
    studentClass: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box {
            Image(
                painter = painterResource(id = R.drawable.graduate),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            IconButton(
                onClick = { /* open image picker later */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change photo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(name, style = MaterialTheme.typography.titleLarge)
        Text(enrollmentId, color = Color.Gray)
        Text(studentClass, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ProfileQuickStats() {
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
            QuickStatItem("CGPA", "7.5")
            QuickStatItem("Attendance", "73%")
            QuickStatItem("Semester", "7")
        }
    }
}

@Composable
fun QuickStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun ProfileStatCard(title: String, value: String) {
    Card(
        modifier = Modifier.width(1.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold)
            Text(title, color = Color.Gray, fontSize = 12.sp)
        }
    }
}


@Composable
fun InfoCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            items.forEach {
                InfoRowModern(it.first, it.second)
            }
        }
    }
}

@Composable
fun InfoRowModern(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Preview
@Composable
fun StudentProfilePreview() {
    StudentProfileScreen(navController = NavHostController(context = LocalContext.current))
}