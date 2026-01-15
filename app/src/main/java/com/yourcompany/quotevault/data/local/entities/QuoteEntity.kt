package com.yourcompany.quotevault.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val category: String,
    val source: String?,
    val createdAt: Long = System.currentTimeMillis()
)