package com.yourcompany.quotevault.domain.model


data class Collection(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val color: String,
    val icon: String,
    val quoteCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)