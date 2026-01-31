package com.buildingbadd.demojc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StudentBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : StudentBottomNavItem(
        route = Routes.STUDENT_HOME,
        title = "Home",
        icon = Icons.Default.Home
    )

    object Attendance : StudentBottomNavItem(
        route = "student_attendance",
        title = "Attendance",
        icon = Icons.Default.Assessment
    )

    object Profile : StudentBottomNavItem(
        route = Routes.STUDENT_PROFILE,
        title = "Profile",
        icon = Icons.Default.AccountCircle
    )
}
