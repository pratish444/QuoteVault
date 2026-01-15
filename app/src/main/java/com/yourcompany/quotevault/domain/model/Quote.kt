package com.yourcompany.quotevault.domain.model


data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val source: String? = null,
    val isFavorite: Boolean = false
)