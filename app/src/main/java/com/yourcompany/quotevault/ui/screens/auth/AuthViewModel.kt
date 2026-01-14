package com.yourcompany.quotevault.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.data.repository.AuthRepository
import com.yourcompany.quotevault.domain.model.User
import com.yourcompany.quotevault.domain.model.UserPreferences
import com.yourcompany.quotevault.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmailConfirmationRequired: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.update { it.copy(isAuthenticated = user != null, user = user) }
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null, isEmailConfirmationRequired = false) }

            when (val result = authRepository.signUp(email, password, displayName)) {
                is Result.Success -> {
                    val user = result.data
                    if (user.id.isEmpty()) {
                        // Email confirmation required
                        _authState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                user = null,
                                isEmailConfirmationRequired = true,
                                error = "Please check your email to confirm your account before logging in."
                            )
                        }
                    } else {
                        _authState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                user = user
                            )
                        }
                    }
                }
                is Result.Error -> {
                    val errorMessage = result.exception.message ?: "Sign up failed"
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = if (errorMessage.contains("check your email")) {
                                errorMessage
                            } else {
                                "Sign up failed: $errorMessage"
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null, isEmailConfirmationRequired = false) }

            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = result.data
                        )
                    }
                }
                is Result.Error -> {
                    val errorMessage = result.exception.message ?: "Sign in failed"

                    // Check if it's an email confirmation issue
                    if (errorMessage.contains("confirm your email", ignoreCase = true)) {
                        _authState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage,
                                isEmailConfirmationRequired = true
                            )
                        }
                    } else {
                        _authState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.resetPassword(email)) {
                is Result.Success -> {
                    _authState.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    fun resendConfirmationEmail(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }

            when (val result = authRepository.resendConfirmationEmail(email)) {
                is Result.Success -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = "Confirmation email sent! Please check your inbox."
                        )
                    }
                }
                is Result.Error -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to resend confirmation email. Please try again."
                        )
                    }
                }
                else -> {}
            }
        }
    }
}