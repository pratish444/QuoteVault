package com.yourcompany.quotevault.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.data.repository.FavoriteRepository
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.ui.components.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Synced())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    val favorites: StateFlow<List<Quote>> = preferencesRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            timber.log.Timber.d("FavoritesViewModel: Loading favorites for userId: ${userId.take(10)}...")
            favoriteRepository.getFavorites(userId)
        }
        .onEach {
            // Update sync status when favorites are loaded
            _syncStatus.value = SyncStatus.Synced()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun removeFavorite(quoteId: String) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val userId = preferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            when (val result = favoriteRepository.removeFavorite(userId, quoteId)) {
                is com.yourcompany.quotevault.utils.Result.Success -> {
                    timber.log.Timber.d("Removed quote from favorites")
                    _message.value = "Removed from favorites"
                    _syncStatus.value = SyncStatus.Synced()
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    timber.log.Timber.e(result.exception, "Failed to remove quote from favorites")
                    _message.value = "Failed to remove from favorites"
                    _syncStatus.value = SyncStatus.Error
                }
                else -> {}
            }
        }
    }

    fun addFavorite(quoteId: String) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val userId = preferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            when (val result = favoriteRepository.addFavorite(userId, quoteId)) {
                is com.yourcompany.quotevault.utils.Result.Success -> {
                    timber.log.Timber.d("Added quote to favorites")
                    _message.value = "Added to favorites"
                    _syncStatus.value = SyncStatus.Synced()
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    timber.log.Timber.e(result.exception, "Failed to add quote to favorites")
                    _message.value = "Failed to add to favorites"
                    _syncStatus.value = SyncStatus.Error
                }
                else -> {}
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _syncStatus.value = SyncStatus.Syncing
            // Favorites are already reactive, just simulate refresh for UI feedback
            kotlinx.coroutines.delay(500)
            _isRefreshing.value = false
            _syncStatus.value = SyncStatus.Synced()
        }
    }
}