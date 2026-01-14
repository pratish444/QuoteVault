package com.yourcompany.quotevault.data.local.entities


import androidx.room.Entity

@Entity(
    tableName = "collection_quotes",
    primaryKeys = ["collectionId", "quoteId"]
)
data class CollectionQuoteEntity(
    val collectionId: String,
    val quoteId: String,
    val addedAt: Long = System.currentTimeMillis()
)