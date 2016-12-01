package io.github.b_lam.resplash.data.api;


import io.github.b_lam.resplash.data.data.SearchCollectionsResult;
import io.github.b_lam.resplash.data.data.SearchPhotosResult;
import io.github.b_lam.resplash.data.data.SearchUsersResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Search api.
 * */

public interface SearchApi {

    @GET("search/photos")
    Call<SearchPhotosResult> searchPhotos(@Query("query") String query,
                                          @Query("page") int page);

    @GET("search/users")
    Call<SearchUsersResult> searchUsers(@Query("query") String query,
                                        @Query("page") int page);

    @GET("search/collections")
    Call<SearchCollectionsResult> searchCollections(@Query("query") String query,
                                                    @Query("page") int page);
}
