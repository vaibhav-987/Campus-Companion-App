package com.buildingbadd.demojc.uiscreen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.buildingbadd.demojc.R


@Composable
fun WelcomeScreen(navController: NavHostController) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // 🔹 Logo (UPG)
            Image(
                painter = painterResource(id = R.drawable.images),
                contentDescription = "College Logo",
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Title
            Text(
                text = "Welcome to Campus Buddy",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Subtitle
            Text(
                text = "LET ACCESS ALL WORK FROM HERE",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Login Button
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Signup Button
            Button(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Sign up")
            }

            // 🔥 This pushes the illustration to the bottom
            Spacer(modifier = Modifier.weight(1f))
        }

        // 🔹 Bottom Illustration
        Image(
            painter = painterResource(id = R.drawable.download),
            contentDescription = "Students Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

    }
}


@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(navController = NavHostController(LocalContext.current))
}




