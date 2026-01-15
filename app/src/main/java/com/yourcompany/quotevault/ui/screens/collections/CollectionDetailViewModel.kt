package com.yourcompany.quotevault.ui.screens.collections


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.repository.CollectionRepository
import com.yourcompany.quotevault.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _collectionId = MutableStateFlow<String?>(null)

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            _collectionId.value = collectionId
            collectionRepository.getCollectionQuotes(collectionId).collect {
                _quotes.value = it
            }
        }
    }

    fun removeQuoteFromCollection(quoteId: String) {
        viewModelScope.launch {
            val collectionId = _collectionId.value ?: return@launch
            val result = collectionRepository.removeQuoteFromCollection(collectionId, quoteId)
            when (result) {
                is com.yourcompany.quotevault.utils.Result.Success -> {
                    Timber.d("Removed quote $quoteId from collection")
                    _message.value = "Removed from collection"
                }
                is com.yourcompany.quotevault.utils.Result.Error -> {
                    Timber.e(result.exception, "Failed to remove quote from collection")
                    _message.value = "Failed to remove from collection"
                }
                else -> {}
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}