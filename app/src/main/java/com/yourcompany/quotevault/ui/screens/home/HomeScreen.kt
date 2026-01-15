package com.yourcompany.quotevault.ui.screens.home


import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yourcompany.quotevault.ui.components.*
import com.yourcompany.quotevault.ui.screens.auth.AuthViewModel
import com.yourcompany.quotevault.utils.CardStyle
import com.yourcompany.quotevault.utils.QuoteShareManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shareManager = remember { QuoteShareManager(context) }

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val quoteOfTheDay by viewModel.quoteOfTheDay.collectAsStateWithLifecycle()
    val quotes = viewModel.quotes.collectAsLazyPagingItems()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    // Share dialog state
    var showShareDialog by remember { mutableStateOf<com.yourcompany.quotevault.domain.model.Quote?>(null) }
    var shareResultMessage by remember { mutableStateOf<String?>(null) }

    // Add to collection dialog state
    var showAddToCollectionDialog by remember { mutableStateOf<com.yourcompany.quotevault.domain.model.Quote?>(null) }

    // Get collections for the dialog
    val collectionsViewModel: com.yourcompany.quotevault.ui.screens.collections.CollectionsViewModel = hiltViewModel()
    val collections by collectionsViewModel.collections.collectAsStateWithLifecycle()

    // Message for collection operations
    val collectionMessage by collectionsViewModel.message.collectAsStateWithLifecycle()
    var snackbarHostState by remember { mutableStateOf(androidx.compose.material3.SnackbarHostState()) }

    // Show snackbar when collection message changes
    LaunchedEffect(collectionMessage) {
        collectionMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            collectionsViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QuoteVault") },
                actions = {
                    IconButton(onClick = { viewModel.syncQuotes() }) {
                        SyncStatusIndicator(status = syncStatus)
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, "Profile")
                    }
                }
            )
        },
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.syncQuotes() },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quote of the Day
                item {
                    quoteOfTheDay?.let { quote ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "QUOTE OF THE DAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "\"${quote.text}\"",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "— ${quote.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Category Chips
                item {
                    CategoryChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }

                // Loading State - Initial Load
                if (quotes.loadState.refresh is LoadState.Loading && quotes.itemCount == 0) {
                    item {
                        LoadingIndicator(
                            message = "Loading quotes...",
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }

                // Empty State - No Results
                if (quotes.loadState.refresh is LoadState.NotLoading && quotes.itemCount == 0) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Inbox,
                            title = "No Quotes Found",
                            description = "Try selecting a different category or sync your quotes",
                            actionText = "Sync Quotes",
                            onActionClick = { viewModel.syncQuotes() },
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }

                // Error State
                if (quotes.loadState.refresh is LoadState.Error && quotes.itemCount == 0) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Error,
                            title = "Error Loading Quotes",
                            description = "Something went wrong. Please try again.",
                            actionText = "Retry",
                            onActionClick = { quotes.retry() },
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }

                // Quotes List
                items(
                    count = quotes.itemCount,
                    key = quotes.itemKey { it.id }
                ) { index ->
                    quotes[index]?.let { quote ->
                        QuoteCard(
                            quote = quote,
                            isFavorite = quote.isFavorite,
                            onFavoriteClick = {
                                authState.user?.let { user ->
                                    viewModel.toggleFavorite(user.id, quote.id)
                                }
                            },
                            onShareClick = { showShareDialog = quote },
                            onAddToCollectionClick = { showAddToCollectionDialog = quote },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Loading State - Append
                if (quotes.loadState.append is LoadState.Loading) {
                    item {
                        LoadingIndicator(
                            message = "Loading more...",
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                // Error State - Append
                if (quotes.loadState.append is LoadState.Error) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Failed to load more quotes",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { quotes.retry() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Text("Retry", color = MaterialTheme.colorScheme.errorContainer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Share dialog
    showShareDialog?.let { quote ->
        QuoteShareDialog(
            onDismiss = { showShareDialog = null },
            onShareAsText = {
                scope.launch {
                    val result = shareManager.executeShare(
                        QuoteShareManager.ShareOption.ShareAsText,
                        quote
                    )
                }
            },
            onShareAsImage = { style ->
                scope.launch {
                    val result = shareManager.executeShare(
                        QuoteShareManager.ShareOption.ShareAsImage(style),
                        quote
                    )
                }
            },
            onSaveAsImage = { style ->
                scope.launch {
                    val result = shareManager.executeShare(
                        QuoteShareManager.ShareOption.SaveAsImage(style),
                        quote
                    )
                }
            }
        )
    }

    // Add to collection dialog
    showAddToCollectionDialog?.let { quote ->
        AddToCollectionDialog(
            collections = collections,
            onDismiss = { showAddToCollectionDialog = null },
            onCreateCollection = {
                showAddToCollectionDialog = null
                // Navigate to create collection screen (would need navigation)
            },
            onAddToCollection = { collectionId ->
                collectionsViewModel.addQuoteToCollection(collectionId, quote.id)
                showAddToCollectionDialog = null
            }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
private fun handleShareResult(result: com.yourcompany.quotevault.utils.ShareResult) {
    // Handle share result - show toast, snackbar, etc.
}