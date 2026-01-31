package com.buildingbadd.demojc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class FacultyBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : FacultyBottomNavItem(
        route = Routes.FACULTY_HOME,
        title = "Home",
        icon = Icons.Default.Home
    )

    object Attendance : FacultyBottomNavItem(
        route = "faculty_attendance",
        title = "Attendance",
        icon = Icons.Default.Edit
    )

    object Profile : FacultyBottomNavItem(
        route = "faculty_profile",
        title = "Profile",
        icon = Icons.Default.AccountCircle
    )
}
