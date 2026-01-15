package com.yourcompany.quotevault.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val color: String,
    val icon: String,
    val createdAt: Long = System.currentTimeMillis()
)