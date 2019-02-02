package com.b_lam.resplash.data.api;


import com.b_lam.resplash.data.model.SearchCollectionsResult;
import com.b_lam.resplash.data.model.SearchPhotosResult;
import com.b_lam.resplash.data.model.SearchUsersResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Search api.
 * */

public interface SearchApi {

    @GET("search/photos")
    Call<SearchPhotosResult> searchPhotos(@Query("query") String query,
                                          @Query("page") Integer page,
                                          @Query("per_page") Integer per_page,
                                          @Query("collections") String collections,
                                          @Query("orientation") String orientation);

    @GET("search/users")
    Call<SearchUsersResult> searchUsers(@Query("query") String query,
                                        @Query("page") Integer page,
                                        @Query("per_page") Integer per_page);

    @GET("search/collections")
    Call<SearchCollectionsResult> searchCollections(@Query("query") String query,
                                                    @Query("page") Integer page,
                                                    @Query("per_page") Integer per_page);
}
