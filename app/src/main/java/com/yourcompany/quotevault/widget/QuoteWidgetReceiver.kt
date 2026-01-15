package com.yourcompany.quotevault.widget


import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.yourcompany.quotevault.UPDATE_WIDGET"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                Timber.d("Received UPDATE_WIDGET action")
                scope.launch {
                    try {
                        QuoteWidget().updateAll(context)
                        Timber.d("Widget updated successfully")
                    } catch (e: Exception) {
                        Timber.e(e, "Error updating widget")
                    }
                }
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                // Handle the default widget update action
                scope.launch {
                    try {
                        QuoteWidget().updateAll(context)
                        Timber.d("Widget updated via system broadcast")
                    } catch (e: Exception) {
                        Timber.e(e, "Error updating widget via system broadcast")
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Trigger initial update
        scope.launch {
            try {
                QuoteWidget().updateAll(context)
                Timber.d("Widget initialized for ${appWidgetIds.size} widgets")
            } catch (e: Exception) {
                Timber.e(e, "Error initializing widget")
            }
        }
    }
}