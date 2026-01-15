package com.yourcompany.quotevault.data.local


import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourcompany.quotevault.data.local.dao.CollectionDao
import com.yourcompany.quotevault.data.local.dao.FavoriteDao
import com.yourcompany.quotevault.data.local.dao.QuoteDao
import com.yourcompany.quotevault.data.local.dao.QuoteCacheDao
import com.yourcompany.quotevault.data.local.entities.*

@Database(
    entities = [
        QuoteEntity::class,
        FavoriteEntity::class,
        CollectionEntity::class,
        CollectionQuoteEntity::class,
        QuoteCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuoteVaultDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun collectionDao(): CollectionDao
    abstract fun quoteCacheDao(): QuoteCacheDao
}