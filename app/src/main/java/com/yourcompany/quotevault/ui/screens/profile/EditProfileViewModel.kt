package com.yourcompany.quotevault.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val displayName: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            currentUser?.let {
                _state.value = _state.value.copy(
                    displayName = it.displayName ?: "",
                    avatarUrl = it.avatarUrl
                )
            }
        }
    }

    fun updateDisplayName(displayName: String) {
        _state.value = _state.value.copy(displayName = displayName)
    }

    fun updateAvatarUrl(avatarUrl: String?) {
        _state.value = _state.value.copy(avatarUrl = avatarUrl)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = authRepository.updateProfile(
                displayName = _state.value.displayName.ifEmpty { null },
                avatarUrl = _state.value.avatarUrl
            )

            when (result) {
                is com.yourcompany.quotevault.utils.Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSaved = true,
                        error = null
                    )
                    onSuccess()
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to update profile"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}