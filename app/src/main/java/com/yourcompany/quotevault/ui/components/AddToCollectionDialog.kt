package com.yourcompany.quotevault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourcompany.quotevault.domain.model.Collection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionDialog(
    collections: List<Collection>,
    onDismiss: () -> Unit,
    onCreateCollection: () -> Unit,
    onAddToCollection: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Collection") },
        text = {
            if (collections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "No collections yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onCreateCollection) {
                            Text("Create Collection")
                        }
                    }
                }
            } else {
                Column {
                    Button(
                        onClick = onCreateCollection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Collection")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(collections) { collection ->
                            CollectionSelectorItem(
                                collection = collection,
                                onClick = { onAddToCollection(collection.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CollectionSelectorItem(
    collection: Collection,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = try {
                Color(android.graphics.Color.parseColor(collection.color))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = try {
                        Color(android.graphics.Color.parseColor(collection.color))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.onSurface
                    }.let { if (it != MaterialTheme.colorScheme.surfaceVariant) Color.White else MaterialTheme.colorScheme.onSurface }
                )
                Text(
                    text = "${collection.quoteCount} quotes",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (try {
                            Color(android.graphics.Color.parseColor(collection.color))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } != MaterialTheme.colorScheme.surfaceVariant
                    ) Color.White.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = if (try {
                        Color(android.graphics.Color.parseColor(collection.color))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } != MaterialTheme.colorScheme.surfaceVariant
                ) Color.White else MaterialTheme.colorScheme.primary
            )
        }
    }
}