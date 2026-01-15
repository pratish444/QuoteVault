package com.yourcompany.quotevault.data.local.dao

import androidx.room.*
import com.yourcompany.quotevault.data.local.entities.CollectionEntity
import com.yourcompany.quotevault.data.local.entities.CollectionQuoteEntity
import com.yourcompany.quotevault.data.local.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCollections(userId: String): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: String): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("""
        SELECT q.* FROM quotes q
        INNER JOIN collection_quotes cq ON q.id = cq.quoteId
        WHERE cq.collectionId = :collectionId
        ORDER BY cq.addedAt DESC
    """)
    fun getCollectionQuotes(collectionId: String): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuoteToCollection(collectionQuote: CollectionQuoteEntity)

    @Query("DELETE FROM collection_quotes WHERE collectionId = :collectionId AND quoteId = :quoteId")
    suspend fun removeQuoteFromCollection(collectionId: String, quoteId: String)

    @Query("SELECT COUNT(*) FROM collection_quotes WHERE collectionId = :collectionId")
    fun getCollectionQuoteCount(collectionId: String): Flow<Int>
}