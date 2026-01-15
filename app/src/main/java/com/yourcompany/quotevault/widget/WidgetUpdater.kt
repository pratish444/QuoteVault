package com.yourcompany.quotevault.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdater {
    private const val PREFS_NAME = "quote_widget_prefs"
    private const val KEY_QUOTE_TEXT = "quote_text"
    private const val KEY_QUOTE_AUTHOR = "quote_author"

    /**
     * Updates the widget with new quote data
     */
    fun updateWidget(context: Context, text: String, author: String) {
        // Save quote data to preferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_QUOTE_TEXT, text)
            .putString(KEY_QUOTE_AUTHOR, author)
            .apply()

        // Trigger widget update in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            QuoteWidget().updateAll(context)
        }
    }

    /**
     * Retrieves the saved quote text
     */
    fun getQuoteText(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_QUOTE_TEXT, "The only way to do great work is to love what you do.") ?: ""
    }

    /**
     * Retrieves the saved quote author
     */
    fun getQuoteAuthor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_QUOTE_AUTHOR, "Steve Jobs") ?: ""
    }

    /**
     * Clears saved quote data
     */
    fun clearQuoteData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}