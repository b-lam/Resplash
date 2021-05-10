package com.b_lam.resplash.data.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.collection.model.CollectionPhotoResult
import com.b_lam.resplash.data.photo.model.Photo
import retrofit2.Response
import retrofit2.http.*

interface CollectionService {

    @GET("collections")
    suspend fun getCollections(
        @Query("page") page: Int?,
        @Query("per_page") per_page: Int?
    ): List<Collection>

    @GET("collections/{id}")
    suspend fun getCollection(
        @Path("id") id: String
    ): Collection

    @GET("collections/{id}/photos")
    suspend fun getCollectionPhotos(
        @Path("id") id: String,
        @Query("page") page: Int?,
        @Query("per_page") per_page: Int?
    ): List<Photo>

    @GET("collections/{id}/related")
    suspend fun getRelatedCollections(
        @Path("id") id: String
    ): List<Collection>

    @POST("collections")
    suspend fun createCollection(
        @Query("title") title: String,
        @Query("description") description: String?,
        @Query("private") private: Boolean?
    ): Collection

    @PUT("collections/{id}")
    suspend fun updateCollection(
        @Path("id") id: String,
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Query("private") private: Boolean?
    ): Collection

    @DELETE("collections/{id}")
    suspend fun deleteCollection(
        @Path("id") id: String
    ): Response<Unit>

    @POST("collections/{collection_id}/add")
    suspend fun addPhotoToCollection(
        @Path("collection_id") collection_id: String,
        @Query("photo_id") photo_id: String
    ): CollectionPhotoResult

    @DELETE("collections/{collection_id}/remove")
    suspend fun removePhotoFromCollection(
        @Path("collection_id") collection_id: String,
        @Query("photo_id") photo_id: String
    ): CollectionPhotoResult
}
