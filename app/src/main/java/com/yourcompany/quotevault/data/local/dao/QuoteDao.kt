package com.yourcompany.quotevault.data.local.dao


import androidx.paging.PagingSource
import androidx.room.*
import com.yourcompany.quotevault.data.local.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): PagingSource<Int, QuoteEntity>

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY createdAt DESC")
    fun getQuotesByCategory(category: String): PagingSource<Int, QuoteEntity>

    @Query("SELECT * FROM quotes WHERE text LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE author LIKE '%' || :author || '%'")
    fun searchByAuthor(author: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :quoteId")
    suspend fun getQuoteById(quoteId: String): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes")
    suspend fun clearAll()

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?
}