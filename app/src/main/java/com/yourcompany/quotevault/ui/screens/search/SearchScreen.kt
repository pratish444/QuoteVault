package com.yourcompany.quotevault.ui.screens.search


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourcompany.quotevault.ui.components.EmptyState
import com.yourcompany.quotevault.ui.components.QuoteCard
import com.yourcompany.quotevault.ui.screens.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search quotes, authors...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (searchQuery.isBlank()) {
            EmptyState(
                icon = Icons.Default.Search,
                title = "Search Quotes",
                description = "Enter keywords to find inspiring quotes",
                modifier = Modifier.padding(padding)
            )
        } else if (searchResults.isEmpty()) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "No Results",
                description = "Try different keywords",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { quote ->
                    QuoteCard(
                        quote = quote,
                        isFavorite = quote.isFavorite,
                        onFavoriteClick = { },
                        onShareClick = { },
                        onAddToCollectionClick = { }
                    )
                }
            }
        }
    }
}