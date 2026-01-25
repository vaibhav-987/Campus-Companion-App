package com.buildingbadd.demojc.navigation


import com.buildingbadd.demojc.uiscreen.faculty.FacultyCreateAssignmentScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buildingbadd.demojc.uiscreen.admin.AdminApprovalScreen
import com.buildingbadd.demojc.uiscreen.auth.LoginScreen
import com.buildingbadd.demojc.uiscreen.auth.PendingApprovalScreen
import com.buildingbadd.demojc.uiscreen.auth.SignupScreen
import com.buildingbadd.demojc.uiscreen.faculty.AssignmentGivenScreen
import com.buildingbadd.demojc.uiscreen.faculty.AssignmentSubmissionsScreen
import com.buildingbadd.demojc.uiscreen.faculty.EvaluateAssignmentScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAssignmentDetailScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAssignmentsScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.StudentDashboard
import com.buildingbadd.demojc.uiscreen.student.StudentNotesScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAssignmentsScreen
import com.buildingbadd.demojc.uiscreen.student.StudentReportsScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyDashboard
import com.buildingbadd.demojc.uiscreen.faculty.FacultyProfileScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyUploadNotesScreen
import com.buildingbadd.demojc.uiscreen.faculty.MarkAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.AssignmentDetailScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceOverviewScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceHistoryScreen
import com.buildingbadd.demojc.uiscreen.student.StudentNotesBySubjectScreen
import com.buildingbadd.demojc.uiscreen.student.StudentProfileScreen
import com.buildingbadd.demojc.uiscreen.student.StudentSubjectsScreen
import com.buildingbadd.demojc.uiscreen.student.StudentTodayLecturesScreen
import com.buildingbadd.demojc.uiscreen.welcome.WelcomeScreen

// 1. Create a top-level Composable function to hold the graph

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /* ---------------- AUTH ---------------- */

        composable(Routes.WELCOME) {
            WelcomeScreen(navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.SIGNUP) {
            SignupScreen(navController)
        }

        composable(Routes.PENDING) {
            PendingApprovalScreen()
        }

        /* ---------------- ADMIN ---------------- */

        composable(Routes.ADMIN_APPROVAL) {
            AdminApprovalScreen(navController)
        }

        /* ---------------- STUDENT ---------------- */

        composable(Routes.STUDENT_HOME) {
            StudentDashboard(navController)
        }

        composable(Routes.STUDENT_ATTENDANCE) {
            StudentAttendanceScreen(navController)
        }

//        composable(Routes.STUDENT_NOTES) {
//            StudentNotesScreen(navController)
//        }

        composable(Routes.STUDENT_ASSIGNMENTS) {
            StudentAssignmentsScreen(navController)
        }

        composable(Routes.STUDENT_LECTURES) {
            StudentTodayLecturesScreen(navController)
        }

        composable(Routes.STUDENT_PROFILE) {
            StudentProfileScreen(navController)
        }

        composable(Routes.STUDENT_ATTENDANCE_OVERVIEW) {
            StudentAttendanceOverviewScreen(navController)
        }

        composable(
            route = "attendance_history/{subjectId}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            StudentAttendanceHistoryScreen(
                navController = navController,
                subjectId = backStackEntry.arguments!!.getString("subjectId")!!
            )
        }


        composable(
            route = "${Routes.STUDENT_ASSIGNMENT_DETAIL}/{assignmentId}",
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.StringType }
            )
        ) {
            AssignmentDetailScreen(navController)
        }

        composable("student_subjects") {
            StudentSubjectsScreen(navController)
        }

        composable(
            route = "student_notes/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) {
            StudentNotesBySubjectScreen(
                navController = navController,
                subjectId = it.arguments?.getString("subjectId")!!
            )
        }

        /* ---------------- FACULTY ---------------- */

        composable(Routes.FACULTY_HOME) {
            FacultyDashboard(navController)
        }

        composable(Routes.FACULTY_ATTENDANCE) {
            FacultyAttendanceScreen(navController)
        }

        composable(Routes.FACULTY_PROFILE) {
            FacultyProfileScreen(navController)
        }

        composable(Routes.FACULTY_CREATE_ASSIGNMENT) {
            FacultyCreateAssignmentScreen(navController)
        }

        composable(Routes.FACULTY_UPLOAD_NOTES) {
            FacultyUploadNotesScreen(navController)
        }

        composable(Routes.FACULTY_ASSIGNMENTS_GIVEN) {
            AssignmentGivenScreen(navController)
        }

        composable(Routes.FACULTY_ASSIGNMENTS) {
            FacultyAssignmentsScreen(navController)
        }

        composable(
            route = "${Routes.FACULTY_ASSIGNMENT_SUBMISSIONS}/{assignmentId}",
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.StringType }
            )
        ) {
            AssignmentSubmissionsScreen(navController, it.arguments?.getString("assignmentId")!!)
        }


        composable(
            route = "faculty_assignment_detail/{assignmentId}",
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.StringType }
            )
        ) {
            FacultyAssignmentDetailScreen(
                navController = navController,
                assignmentId = it.arguments?.getString("assignmentId")!!
            )
        }


        composable(
            route = "evaluate_assignment/{submissionId}",
            arguments = listOf(
                navArgument("submissionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val submissionId = requireNotNull(
                backStackEntry.arguments?.getString("submissionId")
            )

            EvaluateAssignmentScreen(
                navController = navController,
                submissionId = submissionId
            )
        }

        composable(
            route = "mark_attendance/{timetableId}/{className}/{subjectId}/{startTime}"
        ) {
            MarkAttendanceScreen(navController)
        }

        composable(
            route = "student_notes_subject/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) {
            StudentNotesBySubjectScreen(
                navController,
                it.arguments!!.getString("subjectId")!!
            )
        }


    }
}




