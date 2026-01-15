package com.yourcompany.quotevault.data.repository


import com.yourcompany.quotevault.data.local.dao.FavoriteDao
import com.yourcompany.quotevault.data.local.entities.FavoriteEntity
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavorites(userId: String): Flow<List<Quote>> {
        return favoriteDao.getFavoriteQuotes(userId).map { entities ->
            entities.map { entity ->
                Quote(
                    id = entity.id,
                    text = entity.text,
                    author = entity.author,
                    category = entity.category,
                    source = entity.source,
                    isFavorite = true
                )
            }
        }
    }

    override fun isFavorite(userId: String, quoteId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(userId, quoteId)
    }

    override suspend fun addFavorite(userId: String, quoteId: String): Result<Unit> {
        return try {
            val favoriteId = UUID.randomUUID().toString()
            val favorite = FavoriteEntity(
                id = favoriteId,
                userId = userId,
                quoteId = quoteId
            )

            favoriteDao.insertFavorite(favorite)

            // Sync to Supabase
            supabase.from("user_favorites").insert(
                mapOf(
                    "id" to favoriteId,
                    "user_id" to userId,
                    "quote_id" to quoteId
                )
            )

            Timber.d("Added quote $quoteId to favorites")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding favorite")
            Result.Error(e)
        }
    }

    override suspend fun removeFavorite(userId: String, quoteId: String): Result<Unit> {
        return try {
            favoriteDao.deleteFavorite(userId, quoteId)

            // Sync to Supabase
            supabase.from("user_favorites").delete {
                filter {
                    eq("user_id", userId)
                    eq("quote_id", quoteId)
                }
            }

            Timber.d("Removed quote $quoteId from favorites")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing favorite")
            Result.Error(e)
        }
    }

    override suspend fun toggleFavorite(userId: String, quoteId: String): Result<Boolean> {
        return try {
            val exists = favoriteDao.getFavorite(userId, quoteId) != null
            if (exists) {
                removeFavorite(userId, quoteId)
                Result.Success(false)
            } else {
                addFavorite(userId, quoteId)
                Result.Success(true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error toggling favorite")
            Result.Error(e)
        }
    }
}