package com.yourcompany.quotevault.ui.screens.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.yourcompany.quotevault.data.repository.FavoriteRepository
import com.yourcompany.quotevault.data.repository.QuoteRepository
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.ui.components.SyncStatus
import com.yourcompany.quotevault.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _quoteOfTheDay = MutableStateFlow<Quote?>(null)
    val quoteOfTheDay = _quoteOfTheDay.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Offline)
    val syncStatus = _syncStatus.asStateFlow()

    val quotes = selectedCategory.flatMapLatest { category ->
        if (category == "All") {
            quoteRepository.getQuotes()
        } else {
            quoteRepository.getQuotesByCategory(category)
        }
    }.cachedIn(viewModelScope)

    init {
        loadQuoteOfTheDay()
        // Perform initial sync on app launch
        syncQuotes()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            when (val result = quoteRepository.getQuoteOfTheDay()) {
                is Result.Success -> _quoteOfTheDay.value = result.data
                else -> {}
            }
        }
    }

    fun toggleFavorite(userId: String, quoteId: String) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(userId, quoteId)
        }
    }

    fun syncQuotes() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _syncStatus.value = SyncStatus.Syncing
            when (quoteRepository.syncQuotes()) {
                is Result.Success -> {
                    _syncStatus.value = SyncStatus.Synced
                    // Reload quote of the day after sync
                    loadQuoteOfTheDay()
                }
                is Result.Error -> {
                    _syncStatus.value = SyncStatus.Error
                }
                else -> {}
            }
            _isRefreshing.value = false
        }
    }
}