package com.unfair.moment

data class ModeType(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
) {
    companion object {
        val MODES = listOf(
            ModeType(
                id = "essential",
                name = "Essential",
                description = "Start blank and tailor the mode to fit your needs.",
                icon = "⭐",
            ),
            ModeType(
                id = "balance",
                name = "Balance",
                description = "Unlock your potential by shutting out distractions.",
                icon = "⚡",
            ),
            ModeType(
                id = "spring",
                name = "Spring",
                description = "Prioritize your body and mind.",
                icon = "💎",
            ),
        )
    }
}

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: android.graphics.drawable.Drawable? = null,
)

data class Mode(
    val type: ModeType,
    val selectedApps: List<AppInfo> = emptyList(),
    val isDNDActive: Boolean = false,
)


