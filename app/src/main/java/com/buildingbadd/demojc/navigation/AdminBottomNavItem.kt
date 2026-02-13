package com.buildingbadd.demojc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    object Dashboard : AdminBottomNavItem(
        route = Routes.ADMIN_HOME,
        title = "Home",
        icon = Icons.Default.Home
    )

    object Students : AdminBottomNavItem(
        route = Routes.ADMIN_STUDENTS,
        title = "Students",
        icon = Icons.Default.Groups
    )

    object Faculty : AdminBottomNavItem(
        route = Routes.ADMIN_FACULTY,
        title = "Faculty",
        icon = Icons.Default.School
    )

    object Profile : AdminBottomNavItem(
        route = Routes.ADMIN_PROFILE,
        title = "Profile",
        icon = Icons.Default.AccountCircle
    )
}