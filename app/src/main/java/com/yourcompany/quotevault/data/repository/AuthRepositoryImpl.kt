package com.yourcompany.quotevault.data.repository


import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.domain.model.User
import com.yourcompany.quotevault.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val preferencesRepository: UserPreferencesRepository
) : AuthRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableSharedFlow<User?>(
        replay = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    override val currentUser: Flow<User?> = _currentUser.asSharedFlow()

    init {
        // Check and emit current session on initialization
        repositoryScope.launch {
            Timber.d("AuthRepositoryImpl: Initializing and checking for session...")
            emitCurrentUserFromSession()
        }
    }

    private suspend fun emitCurrentUserFromSession() {
        try {
            // Add a small delay to ensure Supabase client is fully initialized
            kotlinx.coroutines.delay(100)
            
            val session = supabase.auth.currentSessionOrNull()
            Timber.d("AuthRepositoryImpl: Session result: ${session != null}")
            
            session?.user?.let { user ->
                try {
                    Timber.d("Saving user ID to preferences: ${user.id.take(10)}...")
                    preferencesRepository.saveUserId(user.id)
                } catch (e: Exception) {
                    Timber.e(e, "Error saving user ID to preferences")
                }
                _currentUser.emit(
                    User(
                        id = user.id,
                        email = user.email ?: "",
                        displayName = user.userMetadata?.get("display_name") as? String
                    )
                )
                Timber.d("AuthRepositoryImpl: Emitted user session for ${user.email}")
            } ?: run {
                Timber.d("No valid session found")
                try {
                    preferencesRepository.clearUserId()
                } catch (e: Exception) {
                    Timber.e(e, "Error clearing user ID from preferences")
                }
                _currentUser.emit(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting current user")
            _currentUser.emit(null)
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("display_name", JsonPrimitive(displayName))
                }
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                preferencesRepository.saveUserId(user.id)

                // Create user profile in Supabase
                try {
                    supabase.from("user_profiles").insert(
                        mapOf(
                            "id" to user.id,
                            "display_name" to displayName
                        )
                    )
                } catch (e: Exception) {
                    // Profile creation might fail if RLS or other issues, but signup succeeded
                    Timber.w(e, "Failed to create user profile")
                }

                // Emit user state to flow
                emitCurrentUserFromSession()

                // Sync preferences from remote (if they exist)
                repositoryScope.launch {
                    try {
                        preferencesRepository.syncFromRemote()
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync preferences from remote after sign up")
                    }
                }

                Result.Success(
                    User(
                        id = user.id,
                        email = email,
                        displayName = displayName
                    )
                )
            } else {
                // User might need email confirmation
                Result.Error(Exception(
                    "Sign up successful. Please check your email to confirm your account before logging in."
                ))
            }
        } catch (e: Exception) {
            Timber.e(e, "Sign up error")
            // Check if the error is due to email confirmation requirement
            val errorMessage = e.message ?: "Sign up failed"
            if (errorMessage.contains("Email not confirmed") || errorMessage.contains("Sign up successful")) {
                // The user was created but needs email confirmation
                Result.Success(
                    User(
                        id = "",
                        email = email,
                        displayName = displayName
                    )
                )
            } else {
                Result.Error(e)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                preferencesRepository.saveUserId(user.id)

                // Emit user state to flow
                emitCurrentUserFromSession()

                // Sync preferences from remote
                repositoryScope.launch {
                    try {
                        preferencesRepository.syncFromRemote()
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync preferences from remote after sign in")
                    }
                }

                Result.Success(
                    User(
                        id = user.id,
                        email = email,
                        displayName = user.userMetadata?.get("display_name") as? String
                    )
                )
            } else {
                Result.Error(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Sign in error")
            // Check for email confirmation error
            val errorMessage = e.message ?: "Sign in failed"
            if (errorMessage.contains("Email not confirmed") ||
                errorMessage.contains("email_not_confirmed") ||
                errorMessage.contains("Email Confirmation")) {
                Result.Error(Exception("Please confirm your email before logging in. Check your inbox for the confirmation link."))
            } else if (errorMessage.contains("Invalid login credentials") ||
                       errorMessage.contains("email_password_mismatch")) {
                Result.Error(Exception("Invalid email or password. Please try again."))
            } else {
                Result.Error(e)
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            preferencesRepository.clearUserId()

            // Emit user state to flow
            emitCurrentUserFromSession()

            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sign out error")
            Result.Error(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Password reset error")
            Result.Error(e)
        }
    }

    override suspend fun resendConfirmationEmail(email: String): Result<Unit> {
        return try {
            // Resend confirmation email using the signUp method with the same email
            // Supabase will send another confirmation email if the user exists
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = "" // Placeholder, won't be used
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Resend confirmation email error")
            // If error indicates user exists, that's expected and email was sent
            if (e.message?.contains("User already registered", ignoreCase = true) == true ||
                e.message?.contains("already been registered", ignoreCase = true) == true ||
                e.message?.contains("duplicate", ignoreCase = true) == true) {
                Result.Success(Unit)
            } else {
                Result.Error(e)
            }
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val user = supabase.auth.currentUserOrNull()
            user?.let {
                User(
                    id = it.id,
                    email = it.email ?: "",
                    displayName = it.userMetadata?.get("display_name") as? String
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting current user")
            null
        }
    }

    override suspend fun syncUserPreferences(): Result<com.yourcompany.quotevault.domain.model.UserPreferences> {
        return try {
            val result = preferencesRepository.syncFromRemote()
            when (result) {
                is com.yourcompany.quotevault.utils.Result.Success<com.yourcompany.quotevault.domain.model.UserPreferences> -> {
                    Timber.d("Successfully synced user preferences from remote")
                    result
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    Timber.e(result.exception, "Failed to sync user preferences from remote")
                    result
                }
                is com.yourcompany.quotevault.utils.Result.Loading -> {
                    Timber.d("Preferences sync is in loading state")
                    result
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing user preferences")
            com.yourcompany.quotevault.utils.Result.Error(e)
        }
    }

    override suspend fun uploadUserPreferences(): Result<Unit> {
        return try {
            val result = preferencesRepository.syncToRemote()
            when (result) {
                is com.yourcompany.quotevault.utils.Result.Success<Unit> -> {
                    Timber.d("Successfully uploaded user preferences to remote")
                    result
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    Timber.e(result.exception, "Failed to upload user preferences to remote")
                    result
                }
                is com.yourcompany.quotevault.utils.Result.Loading -> {
                    Timber.d("Preferences upload is in loading state")
                    result
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading user preferences")
            com.yourcompany.quotevault.utils.Result.Error(e)
        }
    }

    override suspend fun updateProfile(displayName: String?, avatarUrl: String?): Result<User> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
                ?: return Result.Error(Exception("User not logged in"))

            // Update display name in Supabase auth metadata
            if (displayName != null) {
                try {
                    supabase.auth.updateUser {
                        this.data = buildJsonObject {
                            put("display_name", JsonPrimitive(displayName))
                        }
                    }
                    Timber.d("Updated display name to: $displayName")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to update display name in auth")
                }
            }

            // Update user profiles table
            try {
                val updateData = mutableMapOf<String, Any>()
                displayName?.let { updateData["display_name"] = it }
                avatarUrl?.let { updateData["avatar_url"] = it }

                if (updateData.isNotEmpty()) {
                    supabase.from("user_profiles").update(updateData) {
                        filter {
                            eq("id", currentUser.id)
                        }
                    }
                    Timber.d("Updated user_profiles table")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update user_profiles table")
                // Continue anyway, as auth metadata was updated
            }

            // Emit updated user
            emitCurrentUserFromSession()

            // Get the updated user
            val updatedUser = supabase.auth.currentUserOrNull()
            if (updatedUser != null) {
                Result.Success(
                    User(
                        id = updatedUser.id,
                        email = updatedUser.email ?: currentUser.email ?: "",
                        displayName = displayName ?: (updatedUser.userMetadata?.get("display_name") as? String),
                        avatarUrl = avatarUrl?: (currentUser.userMetadata?.get("avatar_url") as? String)
                    )
                )
            } else {
                Result.Error(Exception("Failed to retrieve updated user"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating profile")
            Result.Error(e)
        }
    }
}