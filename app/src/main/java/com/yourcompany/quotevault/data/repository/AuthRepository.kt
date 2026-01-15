package com.yourcompany.quotevault.data.repository

import com.yourcompany.quotevault.domain.model.User
import com.yourcompany.quotevault.domain.model.UserPreferences
import com.yourcompany.quotevault.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, displayName: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun resendConfirmationEmail(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun syncUserPreferences(): Result<UserPreferences>
    suspend fun uploadUserPreferences(): Result<Unit>
    suspend fun updateProfile(displayName: String? = null, avatarUrl: String? = null): Result<User>
}