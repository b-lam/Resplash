package com.b_lam.resplash.domain.collection

import androidx.paging.Pager
import androidx.paging.PagingData
import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.di.Properties
import com.b_lam.resplash.domain.BasePagingSource
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class CollectionRepository(
    private val collectionService: CollectionService,
    private val searchService: SearchService,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getCollections(order: CollectionPagingSource.Companion.Order) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { CollectionPagingSource(collectionService, order) }
    ).flow

    fun searchCollections(query: String): Flow<PagingData<Collection>> = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { SearchCollectionPagingSource(searchService, query) }
    ).flow

    fun getUserCollections(username: String) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { UserCollectionPagingSource(userService, username) }
    ).flow

    suspend fun getUserCollections(username: String, page: Int) =
        safeApiCall(dispatcher) {
            userService.getUserCollections(username, page, Properties.DEFAULT_PAGE_SIZE)
        }

    suspend fun getCollection(collectionId: Int) =
        safeApiCall(dispatcher) { collectionService.getCollection(collectionId) }

    suspend fun createCollection(title: String, description: String?, private: Boolean?) =
        safeApiCall(dispatcher) { collectionService.createCollection(title, description, private) }

    suspend fun updateCollection(id: Int, title: String, description: String?, private: Boolean?) =
        safeApiCall(dispatcher) { collectionService.updateCollection(id, title, description, private) }

    suspend fun deleteCollection(id: Int): Result<Response<Unit>> =
        safeApiCall(dispatcher) { collectionService.deleteCollection(id) }

    suspend fun addPhotoToCollection(collectionId: Int, photoId: String) =
        safeApiCall(dispatcher) { collectionService.addPhotoToCollection(collectionId, photoId) }

    suspend fun removePhotoFromCollection(collectionId: Int, photoId: String) =
        safeApiCall(dispatcher) { collectionService.removePhotoFromCollection(collectionId, photoId) }
}
