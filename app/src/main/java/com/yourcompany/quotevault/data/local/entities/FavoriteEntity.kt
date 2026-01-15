package com.yourcompany.quotevault.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val quoteId: String,
    val createdAt: Long = System.currentTimeMillis()
)