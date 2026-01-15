package com.yourcompany.quotevault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourcompany.quotevault.data.local.entities.QuoteCacheEntity

@Dao
interface QuoteCacheDao {

    @Query("SELECT * FROM quote_cache WHERE id = 'quote_of_the_day'")
    suspend fun getQuoteOfTheDay(): QuoteCacheEntity?

    @Query("SELECT * FROM quote_cache WHERE id = 'quote_of_the_day' AND date = :date")
    suspend fun getQuoteOfTheDayForDate(date: String): QuoteCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuoteCache(quoteCache: QuoteCacheEntity)

    @Query("DELETE FROM quote_cache WHERE id = 'quote_of_the_day'")
    suspend fun clearQuoteOfTheDay()
}