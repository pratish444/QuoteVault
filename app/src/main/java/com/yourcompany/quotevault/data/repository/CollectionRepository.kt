package com.yourcompany.quotevault.data.repository


import com.yourcompany.quotevault.domain.model.Collection
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.Result
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getCollections(userId: String): Flow<List<Collection>>
    fun getCollectionQuotes(collectionId: String): Flow<List<Quote>>
    suspend fun createCollection(userId: String, name: String, description: String?, color: String, icon: String): Result<Collection>
    suspend fun updateCollection(collection: Collection): Result<Unit>
    suspend fun deleteCollection(collectionId: String): Result<Unit>
    suspend fun addQuoteToCollection(collectionId: String, quoteId: String): Result<Unit>
    suspend fun removeQuoteFromCollection(collectionId: String, quoteId: String): Result<Unit>
}