package com.yourcompany.quotevault.ui.theme


import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.yourcompany.quotevault.domain.model.AppTheme
import com.yourcompany.quotevault.domain.model.UserPreferences

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    secondary = SecondaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight
)

@Composable
fun getScaledTypography(fontScale: Float): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (57 * fontScale).sp,
            lineHeight = (64 * fontScale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (45 * fontScale).sp,
            lineHeight = (52 * fontScale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * fontScale).sp,
            lineHeight = (40 * fontScale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (28 * fontScale).sp,
            lineHeight = (36 * fontScale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (24 * fontScale).sp,
            lineHeight = (32 * fontScale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * fontScale).sp,
            lineHeight = (28 * fontScale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * fontScale).sp,
            lineHeight = (16 * fontScale).sp
        )
    )
}

@Composable
fun QuoteVaultTheme(
    userPreferences: UserPreferences? = null,
    darkTheme: Boolean = when (userPreferences?.theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        else -> isSystemInDarkTheme()
    },
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }.let { scheme ->
        // Apply custom accent color if provided
        userPreferences?.accentColor?.let { color ->
            try {
                val accentColor = Color(android.graphics.Color.parseColor(color))
                if (darkTheme) {
                    scheme.copy(primary = accentColor)
                } else {
                    scheme.copy(primary = accentColor)
                }
            } catch (e: Exception) {
                scheme
            }
        } ?: scheme
    }

    // Apply font size scaling from user preferences
    val typography = getScaledTypography(
        fontScale = userPreferences?.fontSize?.scale ?: 1.0f
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}