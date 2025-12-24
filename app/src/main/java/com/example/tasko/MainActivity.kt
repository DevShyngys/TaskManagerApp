package com.example.tasko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.tasko.data.AppContainer
import com.example.tasko.ui.nav.AppNav
import com.example.tasko.ui.theme.TaskoTheme
import com.example.tasko.ui.theme.ThemeViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val container = remember { AppContainer(applicationContext) }
            val nav = rememberAnimatedNavController()
            val themeVm: ThemeViewModel = viewModel()

            TaskoTheme(darkTheme = themeVm.isDark.value) {
                Surface {
                    AppNav(
                        navController = nav,
                        container = container,
                        themeVm = themeVm
                    )
                }
            }
        }

    }
}
