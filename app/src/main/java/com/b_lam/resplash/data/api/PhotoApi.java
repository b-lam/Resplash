package com.b_lam.resplash.data.api;


import com.b_lam.resplash.data.model.LikePhotoResult;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.model.PhotoStats;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Photo api
 **/

public interface PhotoApi {

    @GET("photos")
    Call<List<Photo>> getPhotos(@Query("page") Integer page,
                                @Query("per_page") Integer per_page,
                                @Query("order_by") String order_by);

    @GET("photos/")
    Call<List<Photo>> getCuratedPhotos(@Query("page") Integer page,
                                       @Query("per_page") Integer per_page,
                                       @Query("order_by") String order_by);

    @GET("photos/{id}/statistics")
    Call<PhotoStats> getPhotoStats(@Path("id") String id,
                                   @Query("resolution") String resolution,
                                   @Query("quantity") Integer quantity);

    @GET("categories/{id}/photos")
    Call<List<Photo>> getPhotosInAGivenCategory(@Path("id") Integer id,
                                                @Query("page") Integer page,
                                                @Query("per_page") Integer per_page);

    @POST("photos/{id}/like")
    Call<LikePhotoResult> likeAPhoto(@Path("id") String id);

    @DELETE("photos/{id}/like")
    Call<LikePhotoResult> unlikeAPhoto(@Path("id") String id);

    @GET("photos/{id}")
    Call<Photo> getAPhoto(@Path("id") String id);

    @GET("users/{username}/photos")
    Call<List<Photo>> getUserPhotos(@Path("username") String username,
                                    @Query("page") Integer page,
                                    @Query("per_page") Integer per_page,
                                    @Query("order_by") String order_by);

    @GET("users/{username}/likes")
    Call<List<Photo>> getUserLikes(@Path("username") String username,
                                   @Query("page") Integer page,
                                   @Query("per_page") Integer per_page,
                                   @Query("order_by") String order_by);

    @GET("collections/{id}/photos")
    Call<List<Photo>> getCollectionPhotos(@Path("id") Integer id,
                                          @Query("page") Integer page,
                                          @Query("per_page") Integer per_page);

    @GET("collections/{id}/photos")
    Call<List<Photo>> getCuratedCollectionPhotos(@Path("id") Integer id,
                                                 @Query("page") Integer page,
                                                 @Query("per_page") Integer per_page);

    @GET("photos/random")
    Call<List<Photo>> getRandomPhotos(@Query("collections") Integer collectionsId,
                                      @Query("featured") Boolean featured,
                                      @Query("username") String username,
                                      @Query("query") String query,
                                      @Query("orientation") String orientation,
                                      @Query("count") Integer count);

    @GET("photos/{id}/download")
    Call<ResponseBody> reportDownload(@Path("id") String id);
}
