package com.yourcompany.quotevault.ui.screens.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}