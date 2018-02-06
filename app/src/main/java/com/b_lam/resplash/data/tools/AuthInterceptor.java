package com.b_lam.resplash.data.tools;

import com.b_lam.resplash.Resplash;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Client interceptor.
 * */

public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request;
        if (AuthManager.getInstance().isAuthorized()) {
            request = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + AuthManager.getInstance().getAccessToken())
                    .build();
        } else {
            request = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Client-ID " + Resplash.getAppId(Resplash.getInstance()))
                    .build();
        }
        return chain.proceed(request);
    }
}
