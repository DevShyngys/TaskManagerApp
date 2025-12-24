package com.example.tasko.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = Brand2,
    background = Color(0xFFF7F7FB),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Brand,
    secondary = Brand2,
    background = BgDark,
    surface = CardDark
)

@Composable
fun TaskoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
