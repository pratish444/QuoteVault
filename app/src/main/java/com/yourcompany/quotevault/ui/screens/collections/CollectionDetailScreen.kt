package com.yourcompany.quotevault.ui.screens.collections


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourcompany.quotevault.ui.components.QuoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBackClick: () -> Unit,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(collectionId) {
        viewModel.loadCollection(collectionId)
    }

    // Show snackbar when message changes
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Collection") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(quotes) { quote ->
                QuoteCard(
                    quote = quote,
                    isFavorite = false,
                    onFavoriteClick = { },
                    onShareClick = { },
                    onAddToCollectionClick = { },
                    onRemoveFromCollectionClick = {
                        viewModel.removeQuoteFromCollection(quote.id)
                    },
                    showRemoveButton = true
                )
            }
        }
    }
}