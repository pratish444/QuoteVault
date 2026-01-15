package com.yourcompany.quotevault.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

enum class CardStyle(val displayName: String) {
    MODERN("Modern"),
    CLASSIC("Classic"),
    MINIMAL("Minimal"),
    DARK("Dark"),
    PASTEL("Pastel")
}

data class CardTheme(
    val backgroundColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val cardStyle: CardStyle
)