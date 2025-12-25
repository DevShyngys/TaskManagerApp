package com.example.tasko.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tasko.ui.nav.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    title: String,
    floatingActionButton: @Composable (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (Modifier) -> Unit
) {
    val items = listOf(
        BottomNavItem.Tasks,
        BottomNavItem.Calendar,
        BottomNavItem.Tips,
        BottomNavItem.Settings
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val host = snackbarHostState ?: remember { SnackbarHostState() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(title) }) },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(BottomNavItem.Tasks.route)
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = { floatingActionButton?.invoke() },
        snackbarHost = { SnackbarHost(host) }
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}
