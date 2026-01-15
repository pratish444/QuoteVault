package com.yourcompany.quotevault.workers

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workerFactory: HiltWorkerFactory
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule daily quote notification at the specified time
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleDailyQuote(hour: Int, minute: Int) {
        cancelDailyQuote() // Cancel any existing work first

        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If the scheduled time has already passed today, schedule for tomorrow
        if (scheduledTime.before(currentTime)) {
            scheduledTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Require network to fetch quote
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DailyQuoteWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DAILY_QUOTE_TAG)
            .build()

        workManager.enqueue(workRequest)
        Timber.d("Scheduled daily quote notification for $hour:$minute (in ${delay / 1000 / 60} minutes)")
    }

    /**
     * Schedule periodic daily quote notification
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun schedulePeriodicDailyQuote(hour: Int, minute: Int) {
        cancelDailyQuote() // Cancel any existing work first

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Calculate initial delay to first run
        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        var initialDelay = scheduledTime.timeInMillis - currentTime.timeInMillis
        if (initialDelay < 0) {
            initialDelay += TimeUnit.DAYS.toMillis(1)
        }

        // Use PeriodicWorkRequest with one day interval
        // Note: PeriodicWorkRequest requires a minimum interval of 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<DailyQuoteWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DAILY_QUOTE_TAG)
            .build()

        workManager.enqueue(workRequest)
        Timber.d("Scheduled periodic daily quote notification for $hour:$minute")
    }

    /**
     * Cancel all daily quote work
     */
    fun cancelDailyQuote() {
        workManager.cancelAllWorkByTag(DAILY_QUOTE_TAG)
        Timber.d("Cancelled daily quote notification")
    }

    /**
     * Check if daily quote work is scheduled
     */
    fun isDailyQuoteScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosByTag(DAILY_QUOTE_TAG).get()
        return workInfos.any { !it.state.isFinished }
    }

    companion object {
        const val DAILY_QUOTE_TAG = "daily_quote_notification"
    }
}