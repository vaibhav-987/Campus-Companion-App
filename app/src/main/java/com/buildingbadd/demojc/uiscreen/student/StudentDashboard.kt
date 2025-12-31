package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StudentDashboard(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.TopEnd){
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("welcome") {
                popUpTo(0)
            }

        }) {
            Text("Logout")
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Student Dashboard")
    }
}

@Preview(showSystemUi = true)
@Composable
fun DashboardPreview(){
    StudentDashboard(navController = NavHostController(context = LocalContext.current))
}
