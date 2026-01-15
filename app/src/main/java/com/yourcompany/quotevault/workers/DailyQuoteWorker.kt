package com.yourcompany.quotevault.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourcompany.quotevault.MainActivity
import com.yourcompany.quotevault.QuoteVaultApplication
import com.yourcompany.quotevault.R
import com.yourcompany.quotevault.data.repository.QuoteRepository
import com.yourcompany.quotevault.utils.Result
import com.yourcompany.quotevault.widget.WidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyQuoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val quoteRepository: QuoteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (val result = quoteRepository.getQuoteOfTheDay()) {
            is com.yourcompany.quotevault.utils.Result.Success -> {
                val quote = result.data
                showNotification(quote.text, quote.author)
                updateWidget(quote.text, quote.author)
                Result.success()
            }
            else -> Result.failure()
        }
    }

    private fun showNotification(text: String, author: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            QuoteVaultApplication.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Quote of the Day")
            .setContentText("\"$text\" — $author")
            .setStyle(NotificationCompat.BigTextStyle().bigText("\"$text\" — $author"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    private fun updateWidget(text: String, author: String) {
        // Use WidgetUpdater to save and update the widget
        WidgetUpdater.updateWidget(applicationContext, text, author)
    }
}