package com.yourcompany.quotevault.ui.screens.favorites


import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.ui.components.CardStyleSelectionDialog
import com.yourcompany.quotevault.ui.components.EmptyState
import com.yourcompany.quotevault.ui.components.QuoteCard
import com.yourcompany.quotevault.ui.components.QuoteShareDialog
import com.yourcompany.quotevault.ui.components.SyncStatusIndicator
import com.yourcompany.quotevault.utils.CardStyle
import com.yourcompany.quotevault.utils.QuoteShareManager
import com.yourcompany.quotevault.utils.QuoteShareManager.ShareOption
import com.yourcompany.quotevault.utils.ShareResult
import kotlinx.coroutines.launch

enum class ShareMode { SHARE_AS_IMAGE, SAVE_AS_IMAGE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    val viewModel: FavoritesViewModel = hiltViewModel()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val shareManager = remember { QuoteShareManager(context) }

    var selectedQuote by remember { mutableStateOf<Quote?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var shareMode by remember { mutableStateOf<ShareMode?>(null) }

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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Favorites")
                        Spacer(modifier = Modifier.width(8.dp))
                        SyncStatusIndicator(
                            status = syncStatus,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (favorites.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Favorite,
                    title = "No favorites yet",
                    description = "Save quotes you love and find them here"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites) { quote ->
                        QuoteCard(
                            quote = quote,
                            isFavorite = true,
                            onFavoriteClick = { viewModel.removeFavorite(quote.id) },
                            onShareClick = {
                                selectedQuote = quote
                                showShareDialog = true
                            },
                            onAddToCollectionClick = { }
                        )
                    }
                }
            }
        }
    }

    // Share Dialog
    if (showShareDialog && selectedQuote != null) {
        QuoteShareDialog(
            onDismiss = {
                showShareDialog = false
                selectedQuote = null
                shareMode = null
            },
            onShareAsText = {
                selectedQuote?.let { quote ->
                    scope.launch {
                        shareManager.executeShare(ShareOption.ShareAsText, quote)
                    }
                }
            },
            onShareAsImage = {
                shareMode = ShareMode.SHARE_AS_IMAGE
                showStyleDialog = true
                showShareDialog = false
            },
            onSaveAsImage = {
                shareMode = ShareMode.SAVE_AS_IMAGE
                showStyleDialog = true
                showShareDialog = false
            }
        )
    }

    // Card Style Selection Dialog
    if (showStyleDialog && selectedQuote != null && shareMode != null) {
        CardStyleSelectionDialog(
            onDismiss = {
                showStyleDialog = false
                shareMode = null
            },
            onStyleSelected = { style ->
                selectedQuote?.let { quote ->
                    val mode = shareMode ?: return@CardStyleSelectionDialog
                    scope.launch {
                        val option = when (mode) {
                            ShareMode.SHARE_AS_IMAGE -> ShareOption.ShareAsImage(style)
                            ShareMode.SAVE_AS_IMAGE -> ShareOption.SaveAsImage(style)
                        }
                        shareManager.executeShare(option, quote)
                    }
                }
                showStyleDialog = false
                shareMode = null
                selectedQuote = null
            },
            shareManager = shareManager
        )
    }
}