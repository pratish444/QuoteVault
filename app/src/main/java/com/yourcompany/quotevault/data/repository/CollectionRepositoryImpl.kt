package com.yourcompany.quotevault.data.repository

import com.yourcompany.quotevault.data.local.dao.CollectionDao
import com.yourcompany.quotevault.data.local.entities.CollectionEntity
import com.yourcompany.quotevault.data.local.entities.CollectionQuoteEntity
import com.yourcompany.quotevault.domain.model.Collection
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val collectionDao: CollectionDao
) : CollectionRepository {

    override fun getCollections(userId: String): Flow<List<Collection>> {
        return collectionDao.getCollections(userId).map { entities ->
            entities.map { it.toCollection() }
        }
    }

    override fun getCollectionQuotes(collectionId: String): Flow<List<Quote>> {
        return collectionDao.getCollectionQuotes(collectionId).map { entities ->
            entities.map {
                Quote(
                    id = it.id,
                    text = it.text,
                    author = it.author,
                    category = it.category,
                    source = it.source
                )
            }
        }
    }

    override suspend fun createCollection(
        userId: String,
        name: String,
        description: String?,
        color: String,
        icon: String
    ): Result<Collection> {
        return try {
            val collectionId = UUID.randomUUID().toString()
            val entity = CollectionEntity(
                id = collectionId,
                userId = userId,
                name = name,
                description = description,
                color = color,
                icon = icon
            )

            collectionDao.insertCollection(entity)

            // Sync to Supabase
            supabase.from("collections").insert(
                mapOf(
                    "id" to collectionId,
                    "user_id" to userId,
                    "name" to name,
                    "description" to description,
                    "color" to color,
                    "icon" to icon
                )
            )

            Result.Success(entity.toCollection())
        } catch (e: Exception) {
            Timber.e(e, "Error creating collection")
            Result.Error(e)
        }
    }

    override suspend fun updateCollection(collection: Collection): Result<Unit> {
        return try {
            val entity = CollectionEntity(
                id = collection.id,
                userId = collection.userId,
                name = collection.name,
                description = collection.description,
                color = collection.color,
                icon = collection.icon,
                createdAt = collection.createdAt
            )

            collectionDao.updateCollection(entity)

            // Sync to Supabase
            supabase.from("collections").update(
                mapOf(
                    "name" to collection.name,
                    "description" to collection.description,
                    "color" to collection.color,
                    "icon" to collection.icon
                )
            ) {
                filter {
                    eq("id", collection.id)
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating collection")
            Result.Error(e)
        }
    }

    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        return try {
            val collection = collectionDao.getCollectionById(collectionId)
            if (collection != null) {
                collectionDao.deleteCollection(collection)

                // Sync to Supabase
                supabase.from("collections").delete {
                    filter {
                        eq("id", collectionId)
                    }
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting collection")
            Result.Error(e)
        }
    }

    override suspend fun addQuoteToCollection(collectionId: String, quoteId: String): Result<Unit> {
        return try {
            val entity = CollectionQuoteEntity(
                collectionId = collectionId,
                quoteId = quoteId
            )

            collectionDao.addQuoteToCollection(entity)

            // Sync to Supabase
            supabase.from("collection_quotes").insert(
                mapOf(
                    "collection_id" to collectionId,
                    "quote_id" to quoteId
                )
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding quote to collection")
            Result.Error(e)
        }
    }

    override suspend fun removeQuoteFromCollection(collectionId: String, quoteId: String): Result<Unit> {
        return try {
            collectionDao.removeQuoteFromCollection(collectionId, quoteId)

            // Sync to Supabase
            supabase.from("collection_quotes").delete {
                filter {
                    eq("collection_id", collectionId)
                    eq("quote_id", quoteId)
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing quote from collection")
            Result.Error(e)
        }
    }

    private fun CollectionEntity.toCollection() = Collection(
        id = id,
        userId = userId,
        name = name,
        description = description,
        color = color,
        icon = icon,
        createdAt = createdAt
    )
}