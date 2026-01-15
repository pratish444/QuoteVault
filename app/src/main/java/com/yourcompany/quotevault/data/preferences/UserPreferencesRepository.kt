package com.yourcompany.quotevault.data.preferences


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yourcompany.quotevault.domain.model.AppTheme
import com.yourcompany.quotevault.domain.model.FontSize
import com.yourcompany.quotevault.domain.model.UserPreferences
import com.yourcompany.quotevault.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_preferences")

class UserPreferencesRepository @Inject constructor(
    private val context: Context,
    private val supabase: SupabaseClient
) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val THEME = stringPreferencesKey("theme")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_ID]
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            theme = AppTheme.valueOf(prefs[Keys.THEME] ?: AppTheme.SYSTEM.name),
            accentColor = prefs[Keys.ACCENT_COLOR] ?: "#6366F1",
            fontSize = FontSize.valueOf(prefs[Keys.FONT_SIZE] ?: FontSize.MEDIUM.name),
            fontFamily = prefs[Keys.FONT_FAMILY] ?: "Lora",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: false,
            notificationHour = prefs[Keys.NOTIFICATION_HOUR] ?: 8,
            notificationMinute = prefs[Keys.NOTIFICATION_MINUTE] ?: 0
        )
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
        }
    }

    suspend fun clearUserId() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
        }
    }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    suspend fun updateAccentColor(color: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCENT_COLOR] = color
        }
    }

    suspend fun updateFontSize(fontSize: FontSize) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE] = fontSize.name
        }
    }

    suspend fun updateFontFamily(fontFamily: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_FAMILY] = fontFamily
        }
    }

    suspend fun updateNotifications(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
            prefs[Keys.NOTIFICATION_HOUR] = hour
            prefs[Keys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun syncFromRemote(): Result<UserPreferences> {
        return try {
            val userId = context.dataStore.data.map { prefs ->
                prefs[Keys.USER_ID]
            }.firstOrNull()

            if (userId.isNullOrBlank()) {
                Timber.w("No user ID found, cannot sync preferences from remote")
                return Result.Error(Exception("User not logged in"))
            }

            // Try to fetch preferences from user_preferences table
            val remotePrefs = try {
                supabase.from("user_preferences")
                    .select() {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeSingleOrNull<Map<String, Any>>()
            } catch (e: Exception) {
                Timber.d("user_preferences table might not exist yet: ${e.message}")
                null
            }

            if (remotePrefs != null) {
                // Parse and update local preferences from remote
                try {
                    val theme = remotePrefs["theme"] as? String ?: AppTheme.SYSTEM.name
                    val accentColor = remotePrefs["accent_color"] as? String ?: "#6366F1"
                    val fontSize = remotePrefs["font_size"] as? String ?: FontSize.MEDIUM.name
                    val fontFamily = remotePrefs["font_family"] as? String ?: "Lora"
                    val notificationsEnabled = remotePrefs["notifications_enabled"] as? Boolean ?: false
                    val notificationHour = (remotePrefs["notification_hour"] as? Number)?.toInt() ?: 8
                    val notificationMinute = (remotePrefs["notification_minute"] as? Number)?.toInt() ?: 0

                    // Update local DataStore
                    context.dataStore.edit { prefs ->
                        prefs[Keys.THEME] = theme
                        prefs[Keys.ACCENT_COLOR] = accentColor
                        prefs[Keys.FONT_SIZE] = fontSize
                        prefs[Keys.FONT_FAMILY] = fontFamily
                        prefs[Keys.NOTIFICATIONS_ENABLED] = notificationsEnabled
                        prefs[Keys.NOTIFICATION_HOUR] = notificationHour
                        prefs[Keys.NOTIFICATION_MINUTE] = notificationMinute
                    }

                    val syncedPrefs = UserPreferences(
                        theme = AppTheme.valueOf(theme),
                        accentColor = accentColor,
                        fontSize = FontSize.valueOf(fontSize),
                        fontFamily = fontFamily,
                        notificationsEnabled = notificationsEnabled,
                        notificationHour = notificationHour,
                        notificationMinute = notificationMinute
                    )
                    Timber.d("Successfully synced preferences from remote for user")
                    Result.Success(syncedPrefs)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing remote preferences")
                    Result.Error(e)
                }
            } else {
                // No remote preferences found, return default/current local preferences
                Timber.d("No remote preferences found for user")
                val localPrefs = userPreferencesFlow.first()
                Result.Success(localPrefs)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing preferences from remote")
            Result.Error(e)
        }
    }

    suspend fun syncToRemote(): Result<Unit> {
        return try {
            val userId = context.dataStore.data.map { prefs ->
                prefs[Keys.USER_ID]
            }.firstOrNull()

            if (userId.isNullOrBlank()) {
                Timber.w("No user ID found, cannot sync preferences to remote")
                return Result.Error(Exception("User not logged in"))
            }

            // Get current local preferences
            val localPrefs = userPreferencesFlow.first()

            // Prepare data for upsert
            val prefsData = mapOf(
                "user_id" to userId,
                "theme" to localPrefs.theme.name,
                "accent_color" to localPrefs.accentColor,
                "font_size" to localPrefs.fontSize.name,
                "font_family" to localPrefs.fontFamily,
                "notifications_enabled" to localPrefs.notificationsEnabled,
                "notification_hour" to localPrefs.notificationHour,
                "notification_minute" to localPrefs.notificationMinute,
                "updated_at" to System.currentTimeMillis()
            )

            // Try to upsert to user_preferences table
            try {
                // First try to update if exists, or insert if not
                val existing = try {
                    supabase.from("user_preferences")
                        .select() {
                            filter {
                                eq("user_id", userId)
                            }
                        }
                        .decodeSingleOrNull<Map<String, Any>>()
                } catch (e: Exception) {
                    null
                }

                if (existing != null) {
                    // Update existing record
                    supabase.from("user_preferences").update(prefsData) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    Timber.d("Updated remote preferences for user")
                } else {
                    // Insert new record
                    supabase.from("user_preferences").insert(prefsData)
                    Timber.d("Created remote preferences for user")
                }

                Result.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error syncing preferences to remote (table might not exist)")
                // Don't fail hard - the user_preferences table might not exist in Supabase yet
                // This is expected for new installations and will be created by a migration
                Timber.d("Preferences sync to remote skipped (table may not exist yet)")
                Result.Error(e)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing preferences to remote")
            Result.Error(e)
        }
    }
}