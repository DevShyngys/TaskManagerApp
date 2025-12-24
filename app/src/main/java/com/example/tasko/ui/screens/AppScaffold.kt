package com.example.tasko.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class BottomItem(val label: String, val icon: @Composable () -> Unit, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    currentRoute: String?,
    items: List<BottomItem>,
    onNavigate: (String) -> Unit,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = { item.icon() },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = { floatingActionButton?.invoke() }
    ) { padding ->
        content(
            Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }

}

fun defaultBottomItems(): List<BottomItem> = listOf(
    BottomItem("Tasks", { Icon(Icons.Outlined.Checklist, contentDescription = null) }, "list"),
    BottomItem("Calendar", { Icon(Icons.Outlined.Event, contentDescription = null) }, "calendar"),
    BottomItem("Settings", { Icon(Icons.Outlined.Settings, contentDescription = null) }, "settings")
)
