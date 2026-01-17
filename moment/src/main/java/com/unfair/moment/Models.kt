package com.unfair.moment

data class ModeType(
    val id: String,
    val name: String,
    val description: String,
    val isCustom: Boolean = false,
) {
    companion object {
        val DEFAULT_MODES = listOf(
            ModeType(
                id = "essential",
                name = "Essential",
                description = "Start blank and tailor the mode to fit your needs.",
            )
        )

        val MODES: List<ModeType>
            get() = DEFAULT_MODES
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


