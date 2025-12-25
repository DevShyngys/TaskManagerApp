package com.example.tasko.ui.nav

object Routes {
    const val SPLASH = "splash"
    const val LIST = "list"
    const val ADD = "add"
    const val DETAILS = "details/{id}"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"

    const val TIPS = "tips"


    fun details(id: Long) = "details/$id"
}
