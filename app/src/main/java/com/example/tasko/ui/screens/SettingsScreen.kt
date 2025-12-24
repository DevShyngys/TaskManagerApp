package com.example.tasko.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasko.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(themeVm: ThemeViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    if (themeVm.isDark.value)
                        Icons.Outlined.DarkMode
                    else
                        Icons.Outlined.LightMode,
                    contentDescription = null
                )
                Spacer(Modifier.width(12.dp))
                Text("Dark mode")
            }
            Switch(
                checked = themeVm.isDark.value,
                onCheckedChange = { themeVm.toggle() }
            )
        }
    }
}
