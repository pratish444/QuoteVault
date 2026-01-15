package com.yourcompany.quotevault.ui.screens.search


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.quotevault.data.repository.QuoteRepository
import com.yourcompany.quotevault.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults = _searchQuery
        .debounce(300)
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            quoteRepository.searchQuotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}