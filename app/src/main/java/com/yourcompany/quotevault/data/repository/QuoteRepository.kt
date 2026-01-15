package com.yourcompany.quotevault.data.repository

import androidx.paging.PagingData
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.Result
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuotes(): Flow<PagingData<Quote>>
    fun getQuotesByCategory(category: String): Flow<PagingData<Quote>>
    fun searchQuotes(query: String): Flow<List<Quote>>
    fun searchByAuthor(author: String): Flow<List<Quote>>
    suspend fun getQuoteOfTheDay(): Result<Quote>
    suspend fun syncQuotes(): Result<Unit>
}