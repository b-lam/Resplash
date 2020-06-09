package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import retrofit2.Response

class PhotoRepository(
    private val photoService: PhotoService,
    private val searchService: SearchService,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getPhotos(
        order: PhotoDataSourceFactory.Companion.Order,
        scope: CoroutineScope
    ): Listing<Photo> {
        return PhotoDataSourceFactory(photoService, order, scope).createListing()
    }

    fun getCollectionPhotos(
        collectionId: Int,
        scope: CoroutineScope
    ): Listing<Photo> {
        return CollectionPhotoDataSourceFactory(photoService, collectionId, scope).createListing()
    }

    fun searchPhotos(
        query: String,
        order: SearchPhotoDataSourceFactory.Companion.Order?,
        collections: String?,
        contentFilter: SearchPhotoDataSourceFactory.Companion.ContentFilter?,
        color: SearchPhotoDataSourceFactory.Companion.Color?,
        orientation: SearchPhotoDataSourceFactory.Companion.Orientation?,
        scope: CoroutineScope
    ): Listing<Photo> {
        return SearchPhotoDataSourceFactory(searchService, query, order, collections,
            contentFilter, color, orientation, scope).createListing()
    }

    fun getUserPhotos(
        username: String,
        order: UserPhotoDataSourceFactory.Companion.Order?,
        stats: Boolean,
        resolution: UserPhotoDataSourceFactory.Companion.Resolution?,
        quantity: Int?,
        orientation: UserPhotoDataSourceFactory.Companion.Orientation?,
        scope: CoroutineScope
    ): Listing<Photo> {
        return UserPhotoDataSourceFactory(userService, username, order, stats, resolution,
            quantity, orientation, scope).createListing()
    }

    fun getUserLikes(
        username: String,
        order: UserLikesDataSourceFactory.Companion.Order?,
        orientation: UserLikesDataSourceFactory.Companion.Orientation?,
        scope: CoroutineScope
    ): Listing<Photo> {
        return UserLikesDataSourceFactory(userService, username, order, orientation, scope)
            .createListing()
    }

    suspend fun getPhotoDetails(id: String): Result<Photo> {
        return safeApiCall(dispatcher) { photoService.getPhoto(id) }
    }

    suspend fun getRandomPhoto(
        collectionId: Int? = null,
        featured: Boolean? = false,
        username: String? = null,
        query: String? = null,
        orientation: String? = null,
        contentFilter: String? = null
    ): Result<Photo> {
        return safeApiCall(dispatcher) {
            photoService.getRandomPhotos(
                collectionId, featured, username, query, orientation, contentFilter, 1
            ).first()
        }
    }

    suspend fun trackDownload(id: String) {
        safeApiCall(dispatcher) { photoService.trackDownload(id) }
    }

    suspend fun likePhoto(id: String): Result<ResponseBody> {
        return safeApiCall(dispatcher) { photoService.likeAPhoto(id) }
    }

    suspend fun unlikePhoto(id: String): Result<Response<Unit>> {
        return safeApiCall(dispatcher) { photoService.unlikeAPhoto(id) }
    }
}
