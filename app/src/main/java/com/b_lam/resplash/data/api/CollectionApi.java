package com.b_lam.resplash.data.api;



import com.b_lam.resplash.data.data.ChangeCollectionPhotoResult;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.DeleteCollectionResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Collection api.
 * */

public interface CollectionApi {

    @GET("collections")
    Call<List<Collection>> getAllCollections(@Query("page") Integer page,
                                             @Query("per_page") Integer per_page);

    @GET("collections/curated")
    Call<List<Collection>> getCuratedCollections(@Query("page") Integer page,
                                                 @Query("per_page") Integer per_page);

    @GET("collections/featured")
    Call<List<Collection>> getFeaturedCollections(@Query("page") Integer page,
                                                  @Query("per_page") Integer per_page);

    @GET("collections/{id}/related")
    Call<List<Collection>> getRelatedCollections(@Path("id") String id);

    @GET("users/{username}/collections")
    Call<List<Collection>> getUserCollections(@Path("username") String username,
                                              @Query("page") Integer page,
                                              @Query("per_page") Integer per_page);

    @GET("collections/{id}")
    Call<Collection> getCollection(@Path("id") String id);

    @GET("collections/curated/{id}")
    Call<Collection> getCuratedCollection(@Path("id") String id);

    @POST("collections")
    Call<Collection> createCollection(@Query("title") String title,
                                      @Query("description") String description,
                                      @Query("private") Boolean privateX);

    @POST("collections")
    Call<Collection> createCollection(@Query("title") String title,
                                      @Query("private") Boolean privateX);

    @PUT("collections/{id}")
    Call<Collection> updateCollection(@Path("id") Integer id,
                                      @Query("title") String title,
                                      @Query("description") String description,
                                      @Query("private") Boolean privateX);

    @DELETE("collections/{id}")
    Call<DeleteCollectionResult> deleteCollection(@Path("id") int id);

    @POST("collections/{collection_id}/add")
    Call<ChangeCollectionPhotoResult> addPhotoToCollection(@Path("collection_id") int collection_id,
                                                           @Query("photo_id") String photo_id);

    @DELETE("collections/{collection_id}/remove")
    Call<ChangeCollectionPhotoResult> deletePhotoFromCollection(@Path("collection_id") int collection_id,
                                                                @Query("photo_id") String photo_id);
}
