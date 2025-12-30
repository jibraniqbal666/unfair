package com.unfair.moment

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object ModeSelection : Screen("mode_selection")
    data class AppSelection(val modeTypeId: String) : Screen("app_selection/{modeTypeId}") {
        companion object {
            fun createRoute(modeTypeId: String) = "app_selection/$modeTypeId"
        }
    }
}


