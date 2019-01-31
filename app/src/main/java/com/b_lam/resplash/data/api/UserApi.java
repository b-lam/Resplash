package com.b_lam.resplash.data.api;


import com.b_lam.resplash.data.model.Me;
import com.b_lam.resplash.data.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * User api.
 * */

public interface UserApi {

    @GET("users/{username}")
    Call<User> getUserProfile(@Path("username") String username,
                              @Query("w") Integer w,
                              @Query("h") Integer h);

    @GET("me")
    Call<Me> getMeProfile();

    @PUT("me")
    Call<Me> updateMeProfile(@Query("username") String username,
                             @Query("first_name") String first_name,
                             @Query("last_name") String last_name,
                             @Query("email") String email,
                             @Query("url") String url,
                             @Query("location") String location,
                             @Query("bio") String bio,
                             @Query("instagram_username") String instagram_username);
}
