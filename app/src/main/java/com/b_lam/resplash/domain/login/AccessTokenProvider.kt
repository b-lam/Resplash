package com.b_lam.resplash.domain.login

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.data.authorization.model.AccessToken
import com.b_lam.resplash.data.user.model.Me

class AccessTokenProvider(context: Context) {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    val clientId = if (BuildConfig.DEBUG) {
        if (sharedPreferences.getString(DEBUG_APP_ID_KEY, null).isNullOrBlank()) {
            BuildConfig.DEV_APP_ID
        } else {
            sharedPreferences.getString(DEBUG_APP_ID_KEY, null) ?: BuildConfig.DEV_APP_ID
        }
    } else {
        BuildConfig.RELEASE_APP_ID
    }

    val clientSecret = if (BuildConfig.DEBUG) {
        if (sharedPreferences.getString(DEBUG_APP_SECRET_KEY, null).isNullOrBlank()) {
            BuildConfig.DEV_SECRET
        } else {
            sharedPreferences.getString(DEBUG_APP_SECRET_KEY, null) ?: BuildConfig.DEV_SECRET
        }
    } else {
        BuildConfig.RELEASE_SECRET
    }

    val accessToken: String?
        get() = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)

    val username: String?
        get() = sharedPreferences.getString(USERNAME_KEY, null)

    val email: String?
        get() = sharedPreferences.getString(EMAIL_KEY, null)

    val profilePicture: String?
        get() = sharedPreferences.getString(PROFILE_PICTURE_KEY, null)

    val isAuthorized: Boolean
        get() = !accessToken.isNullOrEmpty()

    fun saveAccessToken(accessToken: AccessToken) = sharedPreferences.edit {
        putString(ACCESS_TOKEN_KEY, accessToken.access_token)
    }

    fun saveUserProfile(me: Me) = sharedPreferences.edit {
        putString(USERNAME_KEY, me.username)
        putString(EMAIL_KEY, me.email)
        putString(PROFILE_PICTURE_KEY, me.profile_image?.large)
    }

    fun reset() = sharedPreferences.edit {
        putString(ACCESS_TOKEN_KEY, null)
        putString(USERNAME_KEY, null)
        putString(EMAIL_KEY, null)
        putString(PROFILE_PICTURE_KEY, null)
    }

    companion object {

        private const val ACCESS_TOKEN_KEY = "access_token"

        private const val USERNAME_KEY = "user_username"
        private const val EMAIL_KEY = "user_email"
        private const val PROFILE_PICTURE_KEY = "user_profile_picture"

        const val DEBUG_APP_ID_KEY = "debug_app_id"
        const val DEBUG_APP_SECRET_KEY = "debug_app_secret"
    }
}
