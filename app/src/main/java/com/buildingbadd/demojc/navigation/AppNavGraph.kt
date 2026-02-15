package com.buildingbadd.demojc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buildingbadd.demojc.uiscreen.admin.AdminAddStudentScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminDashboardScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminFacultyDetailsScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminFacultyListScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminManageStudentsScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminProfileScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminStudentDetailsScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminStudentListScreen
import com.buildingbadd.demojc.uiscreen.admin.AdminYearSelectionScreen
import com.buildingbadd.demojc.uiscreen.auth.LoginScreen
import com.buildingbadd.demojc.uiscreen.auth.SignupScreen
import com.buildingbadd.demojc.uiscreen.faculty.AssignmentGivenScreen
import com.buildingbadd.demojc.uiscreen.faculty.AssignmentSubmissionsScreen
import com.buildingbadd.demojc.uiscreen.faculty.AttendanceDetailScreen
import com.buildingbadd.demojc.uiscreen.faculty.AttendanceSummaryScreen
import com.buildingbadd.demojc.uiscreen.faculty.EvaluateAssignmentScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAssignmentDetailScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAssignmentsScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyAttendanceScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyCreateAssignmentScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyDashboard
import com.buildingbadd.demojc.uiscreen.faculty.FacultyNotesHistoryScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyProfileScreen
import com.buildingbadd.demojc.uiscreen.faculty.FacultyUploadNotesScreen
import com.buildingbadd.demojc.uiscreen.faculty.MarkAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAssignmentDetailScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAssignmentsScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceHistoryScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceOverviewScreen
import com.buildingbadd.demojc.uiscreen.student.StudentAttendanceScreen
import com.buildingbadd.demojc.uiscreen.student.StudentDashboard
import com.buildingbadd.demojc.uiscreen.student.StudentNotesBySubjectScreen
import com.buildingbadd.demojc.uiscreen.student.StudentProfileScreen
import com.buildingbadd.demojc.uiscreen.student.StudentSubjectsScreen
import com.buildingbadd.demojc.uiscreen.student.StudentTodayLecturesScreen
import com.buildingbadd.demojc.uiscreen.welcome.WelcomeScreen


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

        /* ---------------- ADMIN ---------------- */

        composable(Routes.ADMIN_HOME) {
            AdminDashboardScreen(navController)
        }

        composable(Routes.ADMIN_FACULTY) {
            AdminFacultyListScreen(navController)
        }

        composable("admin_faculty_list") {
            AdminFacultyListScreen(navController)
        }

        composable("admin_faculty_details/{facultyId}") { backStackEntry ->
            val facultyId =
                backStackEntry.arguments?.getString("facultyId") ?: ""

            AdminFacultyDetailsScreen(navController, facultyId)
        }

        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(navController)
        }

        composable(Routes.ADMIN_STUDENTS) {
            AdminManageStudentsScreen(navController)
        }


        composable(Routes.ADMIN_ADD_STUDENT) {
            AdminAddStudentScreen(navController)
        }

        composable(
            route = "admin_students_list/{courseId}/{year}"
        ) { backStackEntry ->

            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val year = backStackEntry.arguments?.getString("year") ?: ""

            AdminStudentListScreen(
                navController = navController,
                courseId = courseId,
                year = year
            )
        }

        composable(
            route = "admin_student_details/{enrollmentId}"
        ) { backStackEntry ->

            val enrollmentId =
                backStackEntry.arguments?.getString("enrollmentId") ?: ""

            AdminStudentDetailsScreen(
                navController = navController,
                enrollmentId = enrollmentId
            )
        }

        composable(
            route = "admin_year_list/{courseId}"
        ) { backStackEntry ->

            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""

            AdminYearSelectionScreen(
                navController = navController,
                courseId = courseId
            )
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
            StudentAssignmentDetailScreen(navController)
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



        composable(Routes.ATTENDANCE_SUMMARY) {
            AttendanceSummaryScreen(navController)
        }

        composable(
            route = Routes.ATTENDANCE_DETAIL,
            arguments = listOf(
                navArgument("attendanceId") {
                    type = NavType.StringType
                }
            )
        ) {
            AttendanceDetailScreen(
                navController = navController,
                attendanceId = it.arguments?.getString("attendanceId")!!
            )
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

        composable(Routes.FACULTY_NOTES_HISTORY) {
            FacultyNotesHistoryScreen(navController)
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
            route = "attendance_detail/{attendanceId}"
        ) { backStackEntry ->
            val attendanceId = backStackEntry.arguments?.getString("attendanceId")!!
            AttendanceDetailScreen(
                navController = navController,
                attendanceId = attendanceId
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
            route = "mark_attendance/{timetableId}/{className}/{subjectId}/{startTime}/{facultyId}/{semesterId}",
            arguments = listOf(
                navArgument("timetableId") { type = NavType.StringType },
                navArgument("className") { type = NavType.StringType },
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("startTime") { type = NavType.StringType },
                navArgument("facultyId") { type = NavType.StringType },
                navArgument("semesterId") { type = NavType.StringType }
            )
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




