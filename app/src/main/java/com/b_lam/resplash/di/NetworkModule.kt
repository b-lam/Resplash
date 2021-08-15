package com.b_lam.resplash.di

import com.b_lam.resplash.data.authorization.AuthorizationService
import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.login.AccessTokenInterceptor
import com.b_lam.resplash.domain.login.AccessTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val CONTENT_TYPE = "Content-Type"
private const val APPLICATION_JSON = "application/json"
private const val ACCEPT_VERSION = "Accept-Version"

private const val UNSPLASH_BASE_URL = "https://unsplash.com/"
private const val UNSPLASH_API_BASE_URL = "https://api.unsplash.com/"

val networkModule = module {

    single(createdAtStart = true) { createOkHttpClient(get()) }
    single(createdAtStart = true) { AccessTokenProvider(androidContext()) }
    factory { createAccessTokenInterceptor(get()) }
    factory { createConverterFactory() }
    factory { createService<PhotoService>(get(), get()) }
    factory { createService<CollectionService>(get(), get()) }
    factory { createService<UserService>(get(), get()) }
    factory { createService<SearchService>(get(), get()) }
    factory { createService<AuthorizationService>(get(), get(), UNSPLASH_BASE_URL) }
    factory { createDownloadService(get(), get()) }
}

private fun createOkHttpClient(accessTokenInterceptor: AccessTokenInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addNetworkInterceptor(createHeaderInterceptor())
        .addInterceptor(createHttpLoggingInterceptor())
        .addInterceptor(accessTokenInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

private fun createHeaderInterceptor(): Interceptor {
    return Interceptor { chain ->
        val newRequest = chain.request()
            .newBuilder()
            .addHeader(CONTENT_TYPE, APPLICATION_JSON)
            .addHeader(ACCEPT_VERSION, "v1")
            .build()
        chain.proceed(newRequest)
    }
}

private fun createHttpLoggingInterceptor(): Interceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
        redactHeader("Authorization")
    }
}

private fun createAccessTokenInterceptor(
    accessTokenProvider: AccessTokenProvider
): AccessTokenInterceptor {
    return AccessTokenInterceptor(accessTokenProvider)
}

private fun createConverterFactory(): MoshiConverterFactory {
    return MoshiConverterFactory.create()
}

private inline fun <reified T> createService(
    okHttpClient: OkHttpClient,
    converterFactory: MoshiConverterFactory,
    baseUrl: String = UNSPLASH_API_BASE_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
        .create(T::class.java)
}

private fun createDownloadService(
    accessTokenInterceptor: AccessTokenInterceptor,
    converterFactory: MoshiConverterFactory
): DownloadService {
    val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(createHeaderInterceptor())
        .addInterceptor(createHttpLoggingInterceptor())
        .addInterceptor(accessTokenInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    return Retrofit.Builder()
        .baseUrl(UNSPLASH_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
        .create(DownloadService::class.java)
}

object Properties {

    const val DEFAULT_PAGE_SIZE = 30
}
