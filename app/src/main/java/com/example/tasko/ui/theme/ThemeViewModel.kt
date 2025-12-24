package com.example.tasko.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    var isDark = mutableStateOf(false)
        private set

    fun toggle() {
        isDark.value = !isDark.value
    }
}
