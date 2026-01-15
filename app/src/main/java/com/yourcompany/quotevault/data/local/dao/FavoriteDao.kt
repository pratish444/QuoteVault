package com.yourcompany.quotevault.data.local.dao


import androidx.room.*
import com.yourcompany.quotevault.data.local.entities.FavoriteEntity
import com.yourcompany.quotevault.data.local.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("""
        SELECT q.* FROM quotes q
        INNER JOIN user_favorites f ON q.id = f.quoteId
        WHERE f.userId = :userId
        ORDER BY f.createdAt DESC
    """)
    fun getFavoriteQuotes(userId: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM user_favorites WHERE userId = :userId AND quoteId = :quoteId")
    suspend fun getFavorite(userId: String, quoteId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND quoteId = :quoteId")
    suspend fun deleteFavorite(userId: String, quoteId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND quoteId = :quoteId)")
    fun isFavorite(userId: String, quoteId: String): Flow<Boolean>
}