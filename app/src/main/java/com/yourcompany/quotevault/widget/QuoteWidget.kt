package com.yourcompany.quotevault.widget


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yourcompany.quotevault.MainActivity

class QuoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuoteWidgetContent()
        }
    }
}

@Composable
fun QuoteWidgetContent() {
    val context = LocalContext.current
    val quoteText = WidgetUpdater.getQuoteText(context)
    val quoteAuthor = WidgetUpdater.getQuoteAuthor(context)

    // Check if system is in dark mode
    val isDarkMode = LocalContext.current.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES

    val backgroundColor = when {
        isDarkMode -> ColorProvider(day = Color(0xFF1A1A2E), night = Color(0xFF1A1A2E))
        else -> ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF))
    }

    val textColor = when {
        isDarkMode -> ColorProvider(day = Color(0xFFE6E6E9), night = Color(0xFFE6E6E9))
        else -> ColorProvider(day = Color(0xFF1C1B1F), night = Color(0xFF1C1B1F))
    }

    val primaryColor = when {
        isDarkMode -> ColorProvider(day = Color(0xFF818CF8), night = Color(0xFF818CF8))
        else -> ColorProvider(day = Color(0xFF6366F1), night = Color(0xFF6366F1))
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                actionStartActivity<MainActivity>()
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "QUOTES OF THE DAY",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                ),
                modifier = GlanceModifier.padding(bottom = 12.dp)
            )
            Text(
                text = "\"$quoteText\"",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = textColor
                ),
                modifier = GlanceModifier.padding(bottom = 12.dp)
            )
            Text(
                text = "— $quoteAuthor",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = textColor
                )
            )
        }
    }
}