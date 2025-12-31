package com.buildingbadd.demojc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController // <-- Import NavHostController
import androidx.navigation.compose.NavHost // <-- Import NavHost
import androidx.navigation.compose.composable // <-- Import composable
import com.buildingbadd.demojc.uiscreen.admin.AdminApprovalScreen
import com.buildingbadd.demojc.uiscreen.auth.LoginScreen
import com.buildingbadd.demojc.uiscreen.auth.PendingApprovalScreen
import com.buildingbadd.demojc.uiscreen.auth.SignupScreen
import com.buildingbadd.demojc.uiscreen.student.StudentDashboard
import com.buildingbadd.demojc.uiscreen.teacher.TeacherDashboard
import com.buildingbadd.demojc.uiscreen.welcome.WelcomeScreen

// 1. Create a top-level Composable function to hold the graph

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String // This is the variable we will use
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("welcome") {
            WelcomeScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("signup") {
            SignupScreen(navController)
        }

        composable("pending") {
            PendingApprovalScreen()
        }

        // Ensure these route names match exactly what you send from MainActivity
        composable("student_home") {
            StudentDashboard(navController)
        }

        composable("faculty_home") {
            TeacherDashboard(navController)
        }

        composable("admin_approval") {
            AdminApprovalScreen(navController)
        }
    }
}


