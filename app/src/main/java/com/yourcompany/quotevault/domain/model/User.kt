package com.yourcompany.quotevault.domain.model


data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null
)