package com.yourcompany.quotevault.data.remote.dto


import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class QuoteDto(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val source: String? = null,
    val created_at: String? = null
)