package com.buildingbadd.demojc.navigation

object Routes {

    // Auth
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PENDING = "pending"

    // Student
    const val STUDENT_HOME = "student_home"
    const val STUDENT_ATTENDANCE = "student_attendance"
    const val STUDENT_NOTES = "student_notes"
    const val STUDENT_ASSIGNMENTS = "student_assignments"
    const val STUDENT_LECTURES = "student_lectures"
    const val STUDENT_PROFILE = "student_profile"
    const val STUDENT_ATTENDANCE_OVERVIEW = "student_attendance_overview"
    const val STUDENT_ATTENDANCE_HISTORY = "student_attendance_history"
    const val STUDENT_ASSIGNMENT_DETAIL = "student_assignment_detail"

    // Faculty
    const val FACULTY_HOME = "faculty_home"
    const val FACULTY_ATTENDANCE = "faculty_attendance"
    const val FACULTY_PROFILE = "faculty_profile"
    const val FACULTY_CREATE_ASSIGNMENT = "faculty_create_assignment"
    const val FACULTY_UPLOAD_NOTES = "faculty_upload_notes"
    const val FACULTY_ASSIGNMENTS_GIVEN = "faculty_assignments_given"
    const val FACULTY_ASSIGNMENT_SUBMISSIONS = "faculty_assignment_submissions"
    const val FACULTY_ASSIGNMENTS = "faculty_assignments"
    const val ATTENDANCE_SUMMARY = "attendance_summary"
    const val ATTENDANCE_DETAIL = "attendance_detail/{attendanceId}"
    const val FACULTY_NOTES_HISTORY = "faculty_notes_history"
//    const val ATTENDANCE_SUMMARY = "attendance_summary"
//    const val ATTENDANCE_DETAIL = "attendance_detail/{attendanceId}"



    // Admin
    const val ADMIN_APPROVAL = "admin_approval"
}
