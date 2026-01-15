package com.yourcompany.quotevault.data.remote.dto


import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class CollectionDto(
    val id: String,
    val user_id: String,
    val name: String,
    val description: String? = null,
    val color: String,
    val icon: String,
    val created_at: String? = null
)