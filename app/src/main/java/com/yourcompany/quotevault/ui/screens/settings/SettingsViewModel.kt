package com.yourcompany.quotevault.ui.screens.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.domain.model.AppTheme
import com.yourcompany.quotevault.domain.model.FontSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences = preferencesRepository.userPreferencesFlow

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.updateTheme(theme)
        }
    }

    fun updateAccentColor(color: String) {
        viewModelScope.launch {
            preferencesRepository.updateAccentColor(color)
        }
    }

    fun updateFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            preferencesRepository.updateFontSize(fontSize)
        }
    }

    fun updateFontFamily(fontFamily: String) {
        viewModelScope.launch {
            preferencesRepository.updateFontFamily(fontFamily)
        }
    }

    fun updateNotifications(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNotifications(enabled, hour, minute)
        }
    }
}