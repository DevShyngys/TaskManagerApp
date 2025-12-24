package com.example.tasko.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tasko.data.AppContainer
import com.example.tasko.ui.screens.*
import com.example.tasko.ui.theme.ThemeViewModel
import com.example.tasko.vm.TaskViewModel

@Composable
fun AppNav(
    navController: NavHostController,
    container: AppContainer,
    themeVm: ThemeViewModel
) {
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(container.taskRepository) as T
        }
    }

    val vm: TaskViewModel = viewModel(factory = factory)

    NavHost(navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen {
                navController.navigate(Routes.LIST) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.LIST) {
            val snackbarHostState = remember { SnackbarHostState() }

            MainScaffold(
                navController = navController,
                title = "Tasks",
                snackbarHostState = snackbarHostState,
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate(Routes.ADD) }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add")
                    }
                }
            ) { modifier ->
                ListScreen(
                    vm = vm,
                    onOpen = { id -> navController.navigate(Routes.details(id)) },
                    modifier = modifier,
                    snackbarHostState = snackbarHostState
                )
            }
        }

        composable(Routes.CALENDAR) {
            MainScaffold(
                navController = navController,
                title = "Calendar"
            ) { modifier ->
                CalendarScreen(
                    vm = vm,
                    onOpen = { id -> navController.navigate(Routes.details(id)) },
                    modifier = modifier
                )
            }
        }

        composable(Routes.SETTINGS) {
            MainScaffold(
                navController = navController,
                title = "Settings"
            ) { modifier ->
                Box(modifier) { SettingsScreen(themeVm) }
            }
        }

        composable(Routes.ADD) {
            AddTaskScreen(
                vm = vm,
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: return@composable
            TaskDetailsScreen(
                taskId = id,
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
