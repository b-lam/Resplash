package io.github.b_lam.resplash.Data.Tools;

import java.io.IOException;

import io.github.b_lam.resplash.Resplash;
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
                    .addHeader("Authorization", "Client-ID " + Resplash.APPLICATION_ID)
                    .build();
        }
        return chain.proceed(request);
    }
}
