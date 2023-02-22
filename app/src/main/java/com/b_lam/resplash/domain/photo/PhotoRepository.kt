package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class PhotoRepository(
    private val photoService: PhotoService,
    private val collectionService: CollectionService,
    private val searchService: SearchService,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getPhotos(order: PhotoPagingSource.Companion.Order): Listing<Photo> {
        return PhotoPagingSourceFactory(photoService, order).createListing()
    }

    fun getCollectionPhotos(collectionId: String): Listing<Photo> {
        return CollectionPhotoPagingSourceFactory(collectionService, collectionId).createListing()
    }

    fun searchPhotos(
        query: String,
        order: SearchPhotoPagingSource.Companion.Order?,
        collections: String?,
        contentFilter: SearchPhotoPagingSource.Companion.ContentFilter?,
        color: SearchPhotoPagingSource.Companion.Color?,
        orientation: SearchPhotoPagingSource.Companion.Orientation?
    ): Listing<Photo> {
        return SearchPhotoPagingSourceFactory(searchService, query, order, collections,
            contentFilter, color, orientation).createListing()
    }

    fun getUserPhotos(
        username: String,
        order: UserPhotoPagingSource.Companion.Order?,
        stats: Boolean,
        resolution: UserPhotoPagingSource.Companion.Resolution?,
        quantity: Int?,
        orientation: UserPhotoPagingSource.Companion.Orientation?
    ): Listing<Photo> {
        return UserPhotoPagingSourceFactory(userService, username, order, stats, resolution,
            quantity, orientation).createListing()
    }

    fun getUserLikes(
        username: String,
        order: UserLikesPagingSource.Companion.Order?,
        orientation: UserLikesPagingSource.Companion.Orientation?
    ): Listing<Photo> {
        return UserLikesPagingSourceFactory(userService, username, order, orientation)
            .createListing()
    }

    suspend fun getPhotoDetails(id: String) = safeApiCall(dispatcher) { photoService.getPhoto(id) }

    suspend fun getRandomPhoto(
        collectionId: String? = null,
        featured: Boolean? = false,
        username: String? = null,
        query: String? = null,
        orientation: String? = null,
        contentFilter: String? = null
    ) = safeApiCall(dispatcher) {
        photoService.getRandomPhotos(
            collectionId, featured, username, query, orientation, contentFilter, 1).first()
    }

    suspend fun getRandomPhotos(
        collectionId: String? = null,
        featured: Boolean? = false,
        username: String? = null,
        query: String? = null,
        orientation: String? = null,
        contentFilter: String? = null,
        count: Int = 3
    ) = safeApiCall(dispatcher) {
        photoService.getRandomPhotos(
            collectionId, featured, username, query, orientation, contentFilter, count)
    }

    suspend fun likePhoto(id: String) = safeApiCall(dispatcher) { photoService.likeAPhoto(id) }

    suspend fun unlikePhoto(id: String) = safeApiCall(dispatcher) { photoService.unlikeAPhoto(id) }
}
