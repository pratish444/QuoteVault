package com.yourcompany.quotevault.domain.model


data class UserPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val accentColor: String = "#6366F1",
    val fontSize: FontSize = FontSize.MEDIUM,
    val fontFamily: String = "Lora",
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0
)

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

enum class FontSize(val scale: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f)
}