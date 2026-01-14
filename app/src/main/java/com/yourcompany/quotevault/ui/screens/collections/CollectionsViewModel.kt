package com.yourcompany.quotevault.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.data.repository.CollectionRepository
import com.yourcompany.quotevault.domain.model.Collection
import com.yourcompany.quotevault.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val collections: StateFlow<List<Collection>> = preferencesRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            collectionRepository.getCollections(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun createCollection(name: String, description: String, color: String, icon: String) {
        viewModelScope.launch {
            val userId = preferencesRepository.userIdFlow.firstOrNull() ?: return@launch
            collectionRepository.createCollection(userId, name, description, color, icon)
        }
    }

    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId)
        }
    }

    fun addQuoteToCollection(collectionId: String, quoteId: String) {
        viewModelScope.launch {
            when (val result = collectionRepository.addQuoteToCollection(collectionId, quoteId)) {
                is com.yourcompany.quotevault.utils.Result.Success -> {
                    Timber.d("Added quote to collection")
                    _message.value = "Added to collection"
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    Timber.e(result.exception, "Failed to add quote to collection")
                    _message.value = "Failed to add to collection"
                }
                else -> {}
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}