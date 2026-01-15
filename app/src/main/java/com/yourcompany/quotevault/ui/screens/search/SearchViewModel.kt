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

enum class SearchType {
    ALL, KEYWORD, AUTHOR
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.ALL)
    val searchType = _searchType.asStateFlow()

    val searchResults = combine(_searchQuery, _searchType) { query, type ->
        query to type
    }
        .debounce(300)
        .filter { it.first.isNotBlank() }
        .flatMapLatest { (query, type) ->
            when (type) {
                SearchType.ALL -> quoteRepository.searchQuotes(query)
                SearchType.KEYWORD -> quoteRepository.searchQuotes(query).map { quotes ->
                    quotes.filter { quote -> quote.text.contains(query, ignoreCase = true) }
                }
                SearchType.AUTHOR -> quoteRepository.searchByAuthor(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchType(type: SearchType) {
        _searchType.value = type
    }
}