package com.yourcompany.quotevault.ui.components

sealed class SyncStatus {
    data object Syncing : SyncStatus()
    data object Synced : SyncStatus()
    data object Error : SyncStatus()
    data object Offline : SyncStatus()
}