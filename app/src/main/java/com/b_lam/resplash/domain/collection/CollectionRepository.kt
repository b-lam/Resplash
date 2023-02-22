package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.di.Properties
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

class CollectionRepository(
    private val collectionService: CollectionService,
    private val searchService: SearchService,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getCollections(order: CollectionPagingSource.Companion.Order): Listing<Collection> {
        return CollectionPagingSourceFactory(collectionService, order).createListing()
    }

    fun searchCollections(query: String, ): Listing<Collection> {
        return SearchCollectionPagingSourceFactory(searchService, query).createListing()
    }

    fun getUserCollections(username: String): Listing<Collection> {
        return UserCollectionPagingSourceFactory(userService, username).createListing()
    }

    suspend fun getUserCollections(username: String, page: Int) =
        safeApiCall(dispatcher) {
            userService.getUserCollections(username, page, Properties.DEFAULT_PAGE_SIZE)
        }

    suspend fun getCollection(collectionId: String) =
        safeApiCall(dispatcher) { collectionService.getCollection(collectionId) }

    suspend fun createCollection(title: String, description: String?, private: Boolean?) =
        safeApiCall(dispatcher) { collectionService.createCollection(title, description, private) }

    suspend fun updateCollection(id: String, title: String, description: String?, private: Boolean?) =
        safeApiCall(dispatcher) { collectionService.updateCollection(id, title, description, private) }

    suspend fun deleteCollection(id: String): Result<Response<Unit>> =
        safeApiCall(dispatcher) { collectionService.deleteCollection(id) }

    suspend fun addPhotoToCollection(collectionId: String, photoId: String) =
        safeApiCall(dispatcher) { collectionService.addPhotoToCollection(collectionId, photoId) }

    suspend fun removePhotoFromCollection(collectionId: String, photoId: String) =
        safeApiCall(dispatcher) { collectionService.removePhotoFromCollection(collectionId, photoId) }
}
