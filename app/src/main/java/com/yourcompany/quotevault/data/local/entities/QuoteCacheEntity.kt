package com.yourcompany.quotevault.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_cache")
data class QuoteCacheEntity(
    @PrimaryKey
    val id: String = "quote_of_the_day",
    val date: String, // Format: yyyy-MM-dd
    val quoteId: String,
    val quoteText: String,
    val quoteAuthor: String,
    val quoteCategory: String,
    val source: String?
)