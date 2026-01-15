package com.yourcompany.quotevault.di


import android.content.Context
import androidx.room.Room
import com.yourcompany.quotevault.data.local.QuoteVaultDatabase
import com.yourcompany.quotevault.data.local.dao.CollectionDao
import com.yourcompany.quotevault.data.local.dao.FavoriteDao
import com.yourcompany.quotevault.data.local.dao.QuoteDao
import com.yourcompany.quotevault.data.local.dao.QuoteCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): QuoteVaultDatabase {
        return Room.databaseBuilder(
            context,
            QuoteVaultDatabase::class.java,
            "quote_vault_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideQuoteDao(database: QuoteVaultDatabase): QuoteDao {
        return database.quoteDao()
    }

    @Provides
    fun provideFavoriteDao(database: QuoteVaultDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideCollectionDao(database: QuoteVaultDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    fun provideQuoteCacheDao(database: QuoteVaultDatabase): QuoteCacheDao {
        return database.quoteCacheDao()
    }
}