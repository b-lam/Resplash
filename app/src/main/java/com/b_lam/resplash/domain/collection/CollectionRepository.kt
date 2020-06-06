package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody

class CollectionRepository(
    private val collectionService: CollectionService,
    private val searchService: SearchService,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getCollection(collectionId: Int): Result<Collection> {
        return safeApiCall(dispatcher) { collectionService.getCollection(collectionId) }
    }

    fun getCollections(
        order: CollectionDataSourceFactory.Companion.Order,
        scope: CoroutineScope
    ): Listing<Collection> {
        return CollectionDataSourceFactory(collectionService, order, scope).createListing()
    }

    fun searchCollections(
        query: String,
        scope: CoroutineScope
    ): Listing<Collection> {
        return SearchCollectionDataSourceFactory(searchService, query, scope).createListing()
    }

    fun getUserCollections(
        username: String,
        scope: CoroutineScope
    ): Listing<Collection> {
        return UserCollectionDataSourceFactory(userService, username, scope).createListing()
    }

    suspend fun createCollection(
        title: String,
        description: String?,
        private: Boolean?
    ): Result<Collection> {
        return safeApiCall(dispatcher) {
            collectionService.createCollection(title, description, private)
        }
    }

    suspend fun addPhotoToCollection(
        collectionId: Int,
        photoId: String
    ): Result<ResponseBody> {
        return safeApiCall(dispatcher) { collectionService.addPhotoToCollection(collectionId, photoId) }
    }

    suspend fun removePhotoFromCollection(
        collectionId: Int,
        photoId: String
    ): Result<ResponseBody> {
        return safeApiCall(dispatcher) { collectionService.removePhotoFromCollection(collectionId, photoId) }
    }
}
