package com.b_lam.resplash.data.authorization

import com.b_lam.resplash.data.authorization.model.AccessToken
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthorizationService {

    @POST("oauth/token")
    suspend fun getAccessToken(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("redirect_uri") redirect_uri: String,
        @Query("code") code: String,
        @Query("grant_type") grant_type: String
    ): AccessToken
}
