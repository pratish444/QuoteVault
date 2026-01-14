package com.yourcompany.quotevault.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class FavoriteDto(
    val id: String,
    val user_id: String,
    val quote_id: String,
    val created_at: String? = null
)