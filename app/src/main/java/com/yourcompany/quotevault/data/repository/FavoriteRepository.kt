package com.yourcompany.quotevault.data.repository

import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.Result
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(userId: String): Flow<List<Quote>>
    fun isFavorite(userId: String, quoteId: String): Flow<Boolean>
    suspend fun addFavorite(userId: String, quoteId: String): Result<Unit>
    suspend fun removeFavorite(userId: String, quoteId: String): Result<Unit>
    suspend fun toggleFavorite(userId: String, quoteId: String): Result<Boolean>
}