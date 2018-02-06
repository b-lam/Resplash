package com.b_lam.resplash.data.service;


import android.content.Context;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.api.AuthorizeApi;
import com.b_lam.resplash.data.data.AccessToken;
import com.b_lam.resplash.data.tools.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Authorize service.
 * */

public class AuthorizeService {
    // widget
    private Call call;

    /** <br> data. */

    public void requestAccessToken(Context c, String code, final OnRequestAccessTokenListener l) {
        Call<AccessToken> getAccessToken = buildApi()
                .getAccessToken(
                        Resplash.getAppId(c),
                        Resplash.getSecret(c),
                        "resplash://" + Resplash.UNSPLASH_LOGIN_CALLBACK,
                        code,
                        "authorization_code");
        getAccessToken.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (l != null) {
                    l.onRequestAccessTokenSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                if (l != null) {
                    l.onRequestAccessTokenFailed(call, t);
                }
            }
        });
        call = getAccessToken;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    /** <br> build. */

    public static AuthorizeService getService() {
        return new AuthorizeService();
    }

    private AuthorizeApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(Resplash.UNSPLASH_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((AuthorizeApi.class));
    }

    /** <br> interface. */

    public interface OnRequestAccessTokenListener {
        void onRequestAccessTokenSuccess(Call<AccessToken> call, Response<AccessToken> response);
        void onRequestAccessTokenFailed(Call<AccessToken> call, Throwable t);
    }
}
