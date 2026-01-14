package com.yourcompany.quotevault.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class UserProfileDto(
    val id: String,
    val display_name: String? = null,
    val avatar_url: String? = null,
    val preferences: JsonObject? = null
)