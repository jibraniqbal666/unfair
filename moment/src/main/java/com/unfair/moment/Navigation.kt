package com.unfair.moment

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object ModeSelection : Screen("mode_selection")
    data class ModePreferences(val modeTypeId: String) : Screen("mode_preferences/${modeTypeId}")
    data class AppSelection(val modeTypeId: String) : Screen("app_selection/${modeTypeId}")
}
