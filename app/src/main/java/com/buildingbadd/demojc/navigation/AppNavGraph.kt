package com.buildingbadd.demojc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController // <-- Import NavHostController
import androidx.navigation.compose.NavHost // <-- Import NavHost
import androidx.navigation.compose.composable // <-- Import composable
import com.buildingbadd.demojc.uiscreen.admin.AdminApprovalScreen
import com.buildingbadd.demojc.uiscreen.auth.LoginScreen
import com.buildingbadd.demojc.uiscreen.auth.PendingApprovalScreen
import com.buildingbadd.demojc.uiscreen.auth.SignupScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.StudentDashboard
import com.buildingbadd.demojc.uiscreen.student.StudentNotesScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAssignmentsScreen
import com.buildingbadd.demojc.uiscreen.student.StudentReportsScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyDashboard
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceOverviewScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceHistoryScreen
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
            FacultyDashboard(navController)
        }

        composable("faculty_attendance") {
            FacultyAttendanceScreen(navController)
        }
        composable("admin_approval") {
            AdminApprovalScreen(navController)
        }

        composable("student_attendance") {
            StudentAttendanceScreen(navController)
        }

        composable("student_notes") {
            StudentNotesScreen()
        }

        composable("student_assignments") {
            StudentAssignmentsScreen()
        }

        composable("student_reports") {
            StudentReportsScreen()
        }

        composable("student_attendance") {
            StudentAttendanceOverviewScreen(navController)
        }

        composable("attendance_history/{subjectId}") {
            StudentAttendanceHistoryScreen(
                navController,
                it.arguments?.getString("subjectId")!!
            )
        }



    }
}


