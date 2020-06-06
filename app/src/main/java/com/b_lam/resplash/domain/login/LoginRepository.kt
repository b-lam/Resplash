package com.b_lam.resplash.domain.login

import com.b_lam.resplash.data.authorization.AuthorizationService
import com.b_lam.resplash.data.authorization.model.AccessToken
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.data.user.model.Me
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class LoginRepository(
    private val authorizationService: AuthorizationService,
    private val accessTokenProvider: AccessTokenProvider,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val loginUrl: String
        get() = "https://unsplash.com/oauth/authorize" +
                "?client_id=${accessTokenProvider.clientId}" +
                "&redirect_uri=resplash%3A%2F%2F$unsplashAuthCallback" +
                "&response_type=code" +
                "&scope=public+read_user+write_user+read_photos+write_photos" +
                "+write_likes+write_followers+read_collections+write_collections"

    suspend fun getAccessToken(code: String): Result<AccessToken> {
        val result = safeApiCall(dispatcher) {
            authorizationService.getAccessToken(
                accessTokenProvider.clientId,
                accessTokenProvider.clientSecret,
                redirectUri,
                code,
                grantType
            )
        }

        if (result is Result.Success) {
            accessTokenProvider.saveAccessToken(result.value)
        }

        return result
    }

    suspend fun getMe(): Result<Me> {
        val result = safeApiCall(dispatcher) {
            userService.getUserPrivateProfile()
        }

        if (result is Result.Success) {
            accessTokenProvider.saveUserProfile(result.value)
        }

        return result
    }

    suspend fun updateMe(
        username: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        url: String?,
        instagramUsername: String?,
        location: String?,
        bio: String?
    ): Result<Me> {
        val result = safeApiCall(dispatcher) {
            userService.updateUserPrivateProfile(
                username, firstName, lastName, email, url, instagramUsername, location, bio
            )
        }

        if (result is Result.Success) {
            accessTokenProvider.saveUserProfile(result.value)
        }

        return result
    }

    fun isAuthorized() = accessTokenProvider.isAuthorized

    fun getUsername() = accessTokenProvider.username

    fun getEmail() = accessTokenProvider.email

    fun getProfilePicture() = accessTokenProvider.profilePicture

    fun logout() = accessTokenProvider.reset()

    companion object {

        const val unsplashAuthCallback = "unsplash-auth-callback"

        private const val redirectUri = "resplash://$unsplashAuthCallback"
        private const val grantType = "authorization_code"
    }
}