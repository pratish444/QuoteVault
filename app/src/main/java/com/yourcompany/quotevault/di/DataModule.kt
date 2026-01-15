package com.yourcompany.quotevault.di


import android.content.Context
import com.yourcompany.quotevault.data.local.dao.CollectionDao
import com.yourcompany.quotevault.data.local.dao.FavoriteDao
import com.yourcompany.quotevault.data.local.dao.QuoteDao
import com.yourcompany.quotevault.data.local.dao.QuoteCacheDao
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
import com.yourcompany.quotevault.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context,
        supabaseClient: SupabaseClient
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context, supabaseClient)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        supabaseClient: SupabaseClient,
        preferencesRepository: UserPreferencesRepository
    ): AuthRepository {
        return AuthRepositoryImpl(supabaseClient, preferencesRepository)
    }

    @Provides
    @Singleton
    fun provideQuoteRepository(
        supabaseClient: SupabaseClient,
        quoteDao: QuoteDao,
        quoteCacheDao: QuoteCacheDao
    ): QuoteRepository {
        return QuoteRepositoryImpl(supabaseClient, quoteDao, quoteCacheDao)
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        supabaseClient: SupabaseClient,
        favoriteDao: FavoriteDao
    ): FavoriteRepository {
        return FavoriteRepositoryImpl(supabaseClient, favoriteDao)
    }

    @Provides
    @Singleton
    fun provideCollectionRepository(
        supabaseClient: SupabaseClient,
        collectionDao: CollectionDao
    ): CollectionRepository {
        return CollectionRepositoryImpl(supabaseClient, collectionDao)
    }
}