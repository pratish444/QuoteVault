package com.yourcompany.quotevault.data.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.yourcompany.quotevault.data.local.dao.QuoteDao
import com.yourcompany.quotevault.data.local.dao.QuoteCacheDao
import com.yourcompany.quotevault.data.local.entities.QuoteEntity
import com.yourcompany.quotevault.data.local.entities.QuoteCacheEntity
import com.yourcompany.quotevault.data.remote.dto.QuoteDto
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val quoteDao: QuoteDao,
    private val quoteCacheDao: QuoteCacheDao
) : QuoteRepository {

    override fun getQuotes(): Flow<PagingData<Quote>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { quoteDao.getAllQuotes() }
        ).flow.map { pagingData ->
            pagingData.map { it.toQuote() }
        }
    }

    override fun getQuotesByCategory(category: String): Flow<PagingData<Quote>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { quoteDao.getQuotesByCategory(category) }
        ).flow.map { pagingData ->
            pagingData.map { it.toQuote() }
        }
    }

    override fun searchQuotes(query: String): Flow<List<Quote>> {
        return quoteDao.searchQuotes(query).map { quotes ->
            quotes.map { it.toQuote() }
        }
    }

    override fun searchByAuthor(author: String): Flow<List<Quote>> {
        return quoteDao.searchByAuthor(author).map { quotes ->
            quotes.map { it.toQuote() }
        }
    }

    override suspend fun getQuoteOfTheDay(): Result<Quote> {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        // First, check cache for today's quote
        val cachedQuote = quoteCacheDao.getQuoteOfTheDayForDate(today)
        if (cachedQuote != null) {
            Timber.d("Returning cached quote of the day for $today")
            return Result.Success(
                Quote(
                    id = cachedQuote.quoteId,
                    text = cachedQuote.quoteText,
                    author = cachedQuote.quoteAuthor,
                    category = cachedQuote.quoteCategory,
                    source = cachedQuote.source
                )
            )
        }

        // No cached quote for today, fetch from server
        Timber.d("No cached quote for $today, fetching from server")

        val dailyQuote = supabase.from("daily_quotes")
            .select() {
                filter {
                    eq("date", today)
                }
            }
            .decodeSingleOrNull<Map<String, Any>>()

        val quoteId = dailyQuote?.get("quote_id")?.toString() ?: run {
            // If no daily quote from server, get random from local database
            val randomQuote = quoteDao.getRandomQuote()
            if (randomQuote != null) {
                // Cache the random quote as today's quote
                val cacheEntity = QuoteCacheEntity(
                    id = "quote_of_the_day",
                    date = today,
                    quoteId = randomQuote.id,
                    quoteText = randomQuote.text,
                    quoteAuthor = randomQuote.author,
                    quoteCategory = randomQuote.category,
                    source = randomQuote.source
                )
                quoteCacheDao.insertQuoteCache(cacheEntity)
                return@getQuoteOfTheDay Result.Success(randomQuote.toQuote())
            } else {
                // Return a default inspirational quote when nothing is available
                val defaultQuote = Quote(
                    id = "default-quote",
                    text = "The only way to do great work is to love what you do.",
                    author = "Steve Jobs",
                    category = "Motivation",
                    source = "app"
                )
                Timber.d("No quotes available, returning default quote")
                return@getQuoteOfTheDay Result.Success(defaultQuote)
            }
        }

        val quote = quoteDao.getQuoteById(quoteId)
        if (quote != null) {
            // Cache the quote for today
            val cacheEntity = QuoteCacheEntity(
                id = "quote_of_the_day",
                date = today,
                quoteId = quote.id,
                quoteText = quote.text,
                quoteAuthor = quote.author,
                quoteCategory = quote.category,
                source = quote.source
            )
            quoteCacheDao.insertQuoteCache(cacheEntity)
            Timber.d("Cached quote of the day for $today")
            Result.Success(quote.toQuote())
        } else {
            // Return default quote when quote not found in local DB
            val defaultQuote = Quote(
                id = "default-quote",
                text = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "Motivation",
                source = "app"
            )
            Timber.d("Quote not found, returning default quote")
            Result.Success(defaultQuote)
        }
    } catch (e: Exception) {
        Timber.e(e, "Error getting quote of the day")
        // Return default quote on error instead of error
        val defaultQuote = Quote(
            id = "default-quote",
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs",
            category = "Motivation",
            source = "app"
        )
        Result.Success(defaultQuote)
    }
}

    override suspend fun syncQuotes(): Result<Unit> {
        return try {
            val quotes = supabase.from("quotes")
                .select()
                .decodeList<QuoteDto>()

            val entities = quotes.map { dto: QuoteDto ->
                QuoteEntity(
                    id = dto.id,
                    text = dto.text,
                    author = dto.author,
                    category = dto.category,
                    source = dto.source
                )
            }

            quoteDao.insertQuotes(entities)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing quotes")
            Result.Error(e)
        }
    }

    private fun QuoteEntity.toQuote() = Quote(
        id = id,
        text = text,
        author = author,
        category = category,
        source = source
    )
}