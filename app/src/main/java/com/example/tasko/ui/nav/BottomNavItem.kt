package com.example.tasko.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Tasks : BottomNavItem(
        route = Routes.LIST,
        label = "Tasks",
        icon = Icons.Outlined.Checklist
    )

    object Calendar : BottomNavItem(
        route = Routes.CALENDAR,
        label = "Calendar",
        icon = Icons.Outlined.Event
    )

    object Tips : BottomNavItem(
        route = Routes.TIPS,
        label = "Tips",
        icon = Icons.Outlined.Lightbulb)


    object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        label = "Settings",
        icon = Icons.Outlined.Settings
    )
}
