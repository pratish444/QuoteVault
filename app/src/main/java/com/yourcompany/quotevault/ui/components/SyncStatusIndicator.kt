package com.yourcompany.quotevault.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SyncStatusIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        is SyncStatus.Syncing -> {
            val infiniteTransition = rememberInfiniteTransition(label = "sync")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = "Syncing",
                modifier = modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        is SyncStatus.Synced -> {
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Synced",
                modifier = modifier.size(16.dp),
                tint = Color(0xFF4CAF50)
            )
        }
        is SyncStatus.Error -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Sync Error",
                modifier = modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
        is SyncStatus.Offline -> {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline",
                modifier = modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}