package com.b_lam.resplash.data.topic

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.topic.model.Topic
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TopicService {

    @GET("topics")
    suspend fun getTopics(
        @Query("ids") ids: String?,
        @Query("page") page: Int?,
        @Query("per_page") per_page: Int?,
        @Query("order_by") order_by: String?
    ): List<Topic>

    @GET("topics/{id_or_slug}")
    suspend fun getTopic(
        @Path("id_or_slug") id_or_slug: String
    ): Topic

    @GET("topics/{id_or_slug}/photos")
    suspend fun getTopicPhotos(
        @Path("id_or_slug") id_or_slug: String,
        @Query("page") page: Int?,
        @Query("per_page") per_page: Int?,
        @Query("orientation") orientation: String?,
        @Query("order_by") order_by: String?
    ): List<Photo>
}