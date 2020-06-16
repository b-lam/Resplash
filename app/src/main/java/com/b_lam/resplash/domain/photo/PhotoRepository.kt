package com.b_lam.resplash.domain.photo

import androidx.paging.Pager
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BasePagingSource
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class PhotoRepository(
    private val photoService: PhotoService,
    private val searchService: SearchService,
    private val userService: UserService,
    private val downloadService: DownloadService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getPhotos(order: PhotoPagingSource.Companion.Order) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { PhotoPagingSource(photoService, order) }
    ).flow

    fun getCollectionPhotos(collectionId: Int) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { CollectionPhotoPagingSource(photoService, collectionId) }
    ).flow

    fun searchPhotos(
        query: String,
        order: SearchPhotoPagingSource.Companion.Order?,
        collections: String?,
        contentFilter: SearchPhotoPagingSource.Companion.ContentFilter?,
        color: SearchPhotoPagingSource.Companion.Color?,
        orientation: SearchPhotoPagingSource.Companion.Orientation?
    ) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { SearchPhotoPagingSource(searchService, query, order, collections,
            contentFilter, color, orientation) }
    ).flow

    fun getUserPhotos(
        username: String,
        order: UserPhotoPagingSource.Companion.Order?,
        stats: Boolean,
        resolution: UserPhotoPagingSource.Companion.Resolution?,
        quantity: Int?,
        orientation: UserPhotoPagingSource.Companion.Orientation?
    ) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { UserPhotoPagingSource(userService, username, order, stats,
            resolution, quantity, orientation) }
    ).flow

    fun getUserLikes(
        username: String,
        order: UserLikesPagingSource.Companion.Order?,
        orientation: UserLikesPagingSource.Companion.Orientation?
    ) = Pager(
        config = BasePagingSource.config,
        pagingSourceFactory = { UserLikesPagingSource(userService, username, order, orientation) }
    ).flow

    suspend fun getPhotoDetails(id: String) = safeApiCall(dispatcher) { photoService.getPhoto(id) }

    suspend fun getRandomPhoto(
        collectionId: Int? = null,
        featured: Boolean? = false,
        username: String? = null,
        query: String? = null,
        orientation: String? = null,
        contentFilter: String? = null
    ) = safeApiCall(dispatcher) {
        photoService.getRandomPhotos(
            collectionId, featured, username, query, orientation, contentFilter, 1
        ).first()
    }

    suspend fun likePhoto(id: String) = safeApiCall(dispatcher) { photoService.likeAPhoto(id) }

    suspend fun unlikePhoto(id: String) = safeApiCall(dispatcher) { photoService.unlikeAPhoto(id) }

    suspend fun trackDownload(id: String) = safeApiCall(dispatcher) { downloadService.trackDownload(id) }
}
