package io.github.b_lam.resplash.Data.Api;


import io.github.b_lam.resplash.Data.Data.SearchCollectionsResult;
import io.github.b_lam.resplash.Data.Data.SearchPhotosResult;
import io.github.b_lam.resplash.Data.Data.SearchUsersResult;
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
