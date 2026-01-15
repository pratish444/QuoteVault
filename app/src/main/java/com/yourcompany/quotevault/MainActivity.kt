package com.yourcompany.quotevault



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.ui.QuoteVaultApp
import com.yourcompany.quotevault.ui.theme.QuoteVaultTheme
import com.yourcompany.quotevault.workers.WorkScheduler
import com.yourcompany.quotevault.widget.QuoteWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Update widget when app starts
        scheduleDailyQuoteNotification()
        updateWidget()

        setContent {
            QuoteVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuoteVaultApp()
                }
            }
        }
    }

    private fun scheduleDailyQuoteNotification() {
        lifecycleScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                if (preferences.notificationsEnabled) {
                    if (!workScheduler.isDailyQuoteScheduled()) {
                        workScheduler.schedulePeriodicDailyQuote(
                            hour = preferences.notificationHour,
                            minute = preferences.notificationMinute
                        )
                    }
                } else {
                    workScheduler.cancelDailyQuote()
                }
            }
        }
    }

    private fun updateWidget() {
        lifecycleScope.launch {
            try {
                com.yourcompany.quotevault.widget.QuoteWidget().updateAll(applicationContext)
            } catch (e: Exception) {
                // Widget might not be added yet, that's okay
                // Ignore exception
            }
        }
    }
}