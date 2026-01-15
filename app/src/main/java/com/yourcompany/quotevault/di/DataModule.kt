package com.yourcompany.quotevault.di

import android.content.Context
import com.yourcompany.quotevault.data.preferences.UserPreferencesRepository
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
}